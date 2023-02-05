package uk.gov.hmcts.reform.userprofileapi.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import feign.Response;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceCreator;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseUtil;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.USER_ALREADY_ACTIVE;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileMapper.mapUpdatableFieldsForReInvite;

@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileCreator implements ResourceCreator<UserProfileCreationData> {

    @Value("${auth.idam.client.userid.baseUri:/api/v1/users/}")
    private String sidamGetUri;

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private ValidationHelperService validationHelperService;
    @Value("${syncInterval}")
    String syncInterval;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Autowired
    private IdamFeignClient idamFeignClient;

    private static final String ORIGIN_SRD = "SRD";

    public UserProfile create(UserProfileCreationData profileData, String origin) {

        log.debug("Before userProfileRepository");
        // check if user already in UP then
        Optional<UserProfile>  optionalExistingUserProfile = userProfileRepository.findByEmail(profileData.getEmail()
                .toLowerCase());
        UserProfile userProfile = optionalExistingUserProfile.orElse(null);
        if (null != userProfile) {
            //User already exist in UP for given user email
            persistAudit(IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CONFLICT),
                    HttpStatus.CONFLICT, userProfile);
            log.error("{}:: User already exist in UP for given user email", loggingComponentName);
            userProfile.setIdamRegistrationResponse(HttpStatus.CONFLICT.value());
            return userProfile;
        }
        log.debug("There is no email exist with this already");
        String  userId = UUID.randomUUID().toString();
        final IdamRegistrationInfo idamRegistrationInfo
                = idamService.registerUser(createIdamRegistrationRequest(profileData, userId));
        HttpStatus idamStatus = idamRegistrationInfo.getIdamRegistrationResponse();
        if (idamRegistrationInfo.isSuccessFromIdam()) {
            log.debug("success");
            return persistUserProfileWithAudit(profileData, userId, idamRegistrationInfo.getStatusMessage(),
                    idamRegistrationInfo.getIdamRegistrationResponse());
        } else if (idamRegistrationInfo.isDuplicateUser()) {
            //User already exist in sidam for given email
            log.debug("duplicate user in idam");
            return handleDuplicateUser(profileData, idamRegistrationInfo, origin);
        } else {
            log.debug("exception from idam");
            persistAudit(idamRegistrationInfo.getStatusMessage(), idamStatus, null);
            UserProfileUtil.idam5xxErrorResponse(idamRegistrationInfo.getStatusMessage(), idamStatus);
            throw new IdamServiceException(idamRegistrationInfo.getStatusMessage(), idamStatus);
        }
    }

    public UserProfile reInviteUser(UserProfileCreationData profileData) {

        Optional<UserProfile>  optionalExistingUserProfile = userProfileRepository
                .findByEmail(profileData.getEmail().toLowerCase());
        UserProfile userProfile = validationHelperService.validateReInvitedUser(optionalExistingUserProfile);
        return registerReInvitedUserInSidam(profileData, userProfile);
    }

    @SuppressWarnings("unchecked")
    private UserProfile registerReInvitedUserInSidam(UserProfileCreationData profileData, UserProfile userProfile) {
        String emailKey = "email:";
        String email = profileData.getEmail();
        String emailSearchQuery = emailKey + email;
        Map<String, String> formParams = new HashMap<>();
        formParams.put("query", emailSearchQuery);
        Response response  = idamFeignClient.getUserFeed(formParams);
        logIdamResponse(response);
        if (response.status() == 200) {
            ResponseEntity<Object> responseEntity = JsonFeignResponseUtil.toResponseEntity(response,
                    new TypeReference<Set<IdamFeignClient.User>>() {
                    });
            Set<IdamFeignClient.User> users = (Set<IdamFeignClient.User>) responseEntity.getBody();
            if (users != null && !users.isEmpty() && !users.stream().findFirst().get().getId()
                    .equals(userProfile.getIdamId())) {
                log.info("Do Update Idam Id in Next Sprint");
                userProfile.setIdamId(users.stream().findFirst().get().getId());
                userProfile.setIdamRegistrationResponse(HttpStatus.OK.value());
                userProfile.setStatus(IdamStatus.ACTIVE);
                saveUserProfile(userProfile);
            }
        } else {
            final IdamRegistrationInfo idamRegistrationInfo
                    = idamService.registerUser(createIdamRegistrationRequest(profileData, userProfile.getIdamId()));
            if (idamRegistrationInfo.isSuccessFromIdam()) {
                mapUpdatableFieldsForReInvite(profileData, userProfile);
                userProfile.setIdamRegistrationResponse(idamRegistrationInfo.getIdamRegistrationResponse().value());
                saveUserProfile(userProfile);
                persistAudit(idamRegistrationInfo.getStatusMessage(),
                        idamRegistrationInfo.getIdamRegistrationResponse(), userProfile);
            } else {
                String errorMessage = idamRegistrationInfo.isDuplicateUser() ? String.format(USER_ALREADY_ACTIVE
                        .getErrorMessage(), syncInterval) : idamRegistrationInfo.getStatusMessage();
                persistAuditAndThrowIdamException(errorMessage, idamRegistrationInfo.getIdamRegistrationResponse(),
                        null);
            }
        }
        return userProfile;
    }

    private void logIdamResponse(Response response) {
        log.info("Logging Response from IDAM");
        if (response != null) {
            log.info("Response code from idamClient.getUserFeed {}", response.status());
            if (response.status() != 200 && response.body() != null) {
                log.info("Response body from Idam Client ::{}", response.status());
            }
        }
    }

    private IdamRegisterUserRequest createIdamRegistrationRequest(UserProfileCreationData profileData, String id) {
        return new IdamRegisterUserRequest(profileData.getEmail(), profileData.getFirstName(),
                profileData.getLastName(), id, profileData.getRoles());
    }

    private UserProfile persistUserProfileWithAudit(UserProfileCreationData profileData, String userId,
                                                    String statusMessage, HttpStatus idamStatus) {
        UserProfile userProfile = null;
        if (idamStatus.is2xxSuccessful()) {
            userProfile = new UserProfile(profileData, idamStatus);
            userProfile.setIdamId(userId);
            if (null != profileData.getStatus()) {
                userProfile.setStatus(profileData);
            }
            saveUserProfile(userProfile);
            userProfile.setIdamRegistrationResponse(201);
        }
        persistAudit(statusMessage, idamStatus, userProfile);
        return userProfile;
    }

    private void saveUserProfile(UserProfile userProfile) {
        try {
            userProfileRepository.save(userProfile);
        } catch (Exception ex) {
            persistAudit(ErrorConstants.UNKNOWN_EXCEPTION.toString(), HttpStatus.INTERNAL_SERVER_ERROR, null);
            throw ex;
        }
    }

    private UserProfile handleDuplicateUser(UserProfileCreationData profileData,
                                            IdamRegistrationInfo idamRegistrationInfo, String origin) {

        HttpStatus idamStatus;
        String idamStatusMessage;
        String locationHeader;
        String userId;
        UserProfile userProfile = null;
        IdamRolesInfo idamRolesInfo;
        ResponseEntity<Object> responseEntity = idamRegistrationInfo.getResponse();

        if (responseEntity != null) {
            //get userId from location header
            locationHeader = idamRegistrationInfo.getResponse().getHeaders().getFirst("location");
            userId = nonNull(locationHeader) ? locationHeader.substring(sidamGetUri.length()) : null;
            log.error("{}:: Received existing idam user", loggingComponentName);
            // search with id to get roles
            idamRolesInfo = idamService.fetchUserById(userId);
            idamStatus = idamRolesInfo.getResponseStatusCode();
            idamStatusMessage = idamRolesInfo.getStatusMessage();

            if (idamRolesInfo.isSuccessFromIdam()) {

                updateNamesInSidam(profileData,idamRolesInfo,origin);
                //consolidate XUI + SIDAM roles having unique roles

                Set<String> rolesToUpdate = consolidateRolesFromXuiAndIdam(profileData, idamRolesInfo);
                //if roles are same what SIDAM has and XUI sent then skip SIDAM add roles call
                if (!CollectionUtils.isEmpty(rolesToUpdate)) {
                    idamRolesInfo = addIdamRoles(rolesToUpdate, userId);
                    idamStatus = idamRolesInfo.getResponseStatusCode();
                    idamStatusMessage = idamRolesInfo.getStatusMessage();
                    if (!idamRolesInfo.isSuccessFromIdam()) {
                        log.error("{}:: failed sidam add roles POST call for the given userId", loggingComponentName);
                        persistAuditAndThrowIdamException(idamStatusMessage, idamStatus, null);
                    }
                }
                // for success make status = 201
                idamStatus = HttpStatus.CREATED;
                idamStatusMessage = IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus);
                userProfile = persistUserProfileWithAudit(profileData, userId, idamStatusMessage, idamStatus);
            } else {
                log.error("{}:: failed sidam GET call for the given userId", loggingComponentName);
                persistAuditAndThrowIdamException(idamStatusMessage, idamStatus, null);
            }
        } else {
            log.error("{}:: Did not get location header", loggingComponentName);
            idamStatus = HttpStatus.UNAUTHORIZED;
            persistAuditAndThrowIdamException(IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus),
                    idamStatus, null);
        }
        return userProfile;
    }

    private void persistAudit(String message, HttpStatus idamStatus, UserProfile userProfile) {
        Audit audit = new Audit(idamStatus.value(), message, ResponseSource.API, userProfile);
        auditRepository.save(audit);
    }

    private void persistAuditAndThrowIdamException(String message, HttpStatus idamStatus, UserProfile userProfile) {
        persistAudit(message, idamStatus, userProfile);
        UserProfileUtil.idam5xxErrorResponse(message, idamStatus);
        throw new  IdamServiceException(message,idamStatus);
    }

    public void updateInputRequestWithLatestSidamUserInfo(UserProfileCreationData profileData,
                                                          IdamRolesInfo idamRolesInfo) {
        profileData.setStatus(IdamStatusResolver.resolveIdamStatus(idamRolesInfo));
        if (idamRolesInfo.getEmail() != null) {
            profileData.setEmail(idamRolesInfo.getEmail());
        }
        if (idamRolesInfo.getForename() != null) {
            profileData.setFirstName(idamRolesInfo.getForename());
        }
        if (idamRolesInfo.getSurname() != null) {
            profileData.setLastName(idamRolesInfo.getSurname());
        }
    }

    private void updateNamesInSidam(UserProfileCreationData profileData, IdamRolesInfo idamRolesInfo, String origin) {
        if (ORIGIN_SRD.equals(origin)) {
            updateSidamUserInfoWithUserProfileDetails(profileData, idamRolesInfo);

        } else {
            // set updated user info
            updateInputRequestWithLatestSidamUserInfo(profileData, idamRolesInfo);
        }
    }

    private void updateSidamUserInfoWithUserProfileDetails(UserProfileCreationData profileData,
                                                          IdamRolesInfo idamRolesInfo) {
        profileData.setStatus(IdamStatusResolver.resolveIdamStatus(idamRolesInfo));
        if (!(profileData.getFirstName().equals(idamRolesInfo.getForename()) && profileData.getLastName()
                .equals(idamRolesInfo.getSurname()))) {
            UpdateUserDetails updateUserDetails = new UpdateUserDetails(profileData.getFirstName(),
                    profileData.getLastName(), "ACTIVE".equals(profileData.getStatus().toString()));
            AttributeResponse attributeResponse = idamService
                    .updateUserDetails(updateUserDetails, idamRolesInfo.getId());
            HttpStatus status = HttpStatus.valueOf(attributeResponse.getIdamStatusCode());
            if (!status.is2xxSuccessful()) {
                log.error("{}:: failed sidam update names as per userprofile POST call for the given origin",
                        loggingComponentName);
                persistAuditAndThrowIdamException(attributeResponse.getIdamMessage(), status, null);
            }

        }
    }

    public Set<String> consolidateRolesFromXuiAndIdam(UserProfileCreationData profileData,
                                                      IdamRolesInfo idamRolesInfo) {
        Optional<List<String>> roles = Optional.ofNullable(idamRolesInfo.getRoles());
        List<String> idamRoles = roles.isPresent() ? roles.get() : new ArrayList<>();
        List<String> xuiRoles = profileData.getRoles();
        //if both list are same roles then return empty HashSet
        if (new HashSet<String>(idamRoles).equals(new HashSet<String>(xuiRoles))) {
            return new HashSet<>();
        }
        xuiRoles.removeAll(idamRoles);
        return new HashSet<>(xuiRoles);
    }

    private IdamRolesInfo addIdamRoles(Set<String> rolesToUpdate, String userId) {

        return idamService.addUserRoles(createIdamRolesRequest(rolesToUpdate), userId);
    }

    public Set<Map<String,String>> createIdamRolesRequest(Set<String> rolesToUpdate) {
        Set<Map<String, String>> roles = new HashSet<>();
        rolesToUpdate.forEach(role -> {
            Map<String, String> rolesMap = new HashMap<>();
            rolesMap.put("name", role);
            roles.add(rolesMap);
        });
        return roles;
    }


}
