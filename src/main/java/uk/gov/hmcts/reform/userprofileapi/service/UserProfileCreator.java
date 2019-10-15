package uk.gov.hmcts.reform.userprofileapi.service;

import java.net.URI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@Service
@Slf4j
public class UserProfileCreator implements ResourceCreator<CreateUserProfileData> {

    @Value("${auth.idam.client.userid.baseUri:/api/v1/users/}")
    private String sidamGetUri;

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    Map<Map<String, Boolean>, IdamStatus> idamStatusResolverMap;

    public UserProfile create(CreateUserProfileData profileData) {

        // check if user already in UP then
        Optional<UserProfile>  optionalExistingUserProfile = userProfileRepository.findByEmail(profileData.getEmail().toLowerCase());
        UserProfile userProfile = optionalExistingUserProfile.orElse(null);
        if (null != userProfile) {
            log.info("User already exist in UP for user email : " + profileData.getEmail());
            persistAuditAndThrowIdamException(IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CONFLICT), HttpStatus.CONFLICT, userProfile);
        }

        String  userId = UUID.randomUUID().toString();
        final IdamRegistrationInfo idamRegistrationInfo = idamService.registerUser(createIdamRegistrationRequest(profileData, userId));
        HttpStatus idamStatus = idamRegistrationInfo.getIdamRegistrationResponse();
        if (idamRegistrationInfo.isSuccessFromIdam()) {
            return persistUserProfileWithAudit(profileData, userId, idamRegistrationInfo.getStatusMessage(), idamRegistrationInfo.getIdamRegistrationResponse());
        } else if (idamRegistrationInfo.isDuplicateUser()) {
            log.info("User already exist in sidam for eamil : " + profileData.getEmail());
            return handleDuplicateUser(profileData, idamRegistrationInfo);
        } else {
            persistAudit(idamRegistrationInfo.getStatusMessage(), idamStatus, null);
            throw new IdamServiceException(idamRegistrationInfo.getStatusMessage(), idamStatus);
        }
    }

    public IdamRegisterUserRequest createIdamRegistrationRequest(CreateUserProfileData profileData, String id) {
        return new IdamRegisterUserRequest(profileData.getEmail(), profileData.getFirstName(), profileData.getLastName(), id, profileData.getRoles());
    }

    private UserProfile persistUserProfileWithAudit(CreateUserProfileData profileData, String userId, String stausMessage, HttpStatus idamStatus) {
        UserProfile userProfile = null;
        if (idamStatus.is2xxSuccessful()) {
            userProfile = new UserProfile(profileData, idamStatus);
            userProfile.setIdamId(userId);
            if (profileData.getStatus() != null) {
                userProfile.setStatus(profileData.getStatus());
            }

            try {
                userProfile = userProfileRepository.save(userProfile);
            } catch (Exception ex) {
                persistAudit(ErrorConstants.UNKNOWN_EXCEPTION.toString(), HttpStatus.INTERNAL_SERVER_ERROR, null);
                throw ex;
            }
        }
        persistAudit(stausMessage, idamStatus, userProfile);
        return userProfile;
    }

    private UserProfile handleDuplicateUser(CreateUserProfileData profileData, IdamRegistrationInfo idamRegistrationInfo) {

        HttpStatus idamStatus;
        String idamStatusMessage;
        URI userIdUri;
        String userId;
        UserProfile userProfile = null;
        IdamRolesInfo idamRolesInfo;
        ResponseEntity responseEntity = idamRegistrationInfo.getResponse();

        if (responseEntity != null) {
            //get userId from location header
            userIdUri = idamRegistrationInfo.getResponse().getHeaders().getLocation();
            userId = userIdUri != null ? userIdUri.toString().substring(sidamGetUri.length()) : null;
            log.error("Received existing idam userId : " + userId);
            // search with id to get roles
            idamRolesInfo = idamService.fetchUserById(userId);
            idamStatus = idamRolesInfo.getResponseStatusCode();
            idamStatusMessage = idamRolesInfo.getStatusMessage();

            if (idamRolesInfo.isSuccessFromIdam()) {
                // set updated user info
                updateInputRequestWithLatestSidamUserInfo(profileData, idamRolesInfo);

                //consolidate XUI + SIDAM roles having unique roles
                Set<String> rolesToUpdate = consolidateRolesFromXuiAndIdam(profileData, idamRolesInfo);
                //if roles are same what SIDAM has and XUI sent then skip SIDAM add roles call
                if (! CollectionUtils.isEmpty(rolesToUpdate)) {
                    idamRolesInfo = addIdamRoles(rolesToUpdate, userId);
                    idamStatus = idamRolesInfo.getResponseStatusCode();
                    idamStatusMessage = idamRolesInfo.getStatusMessage();
                    if (!idamRolesInfo.isSuccessFromIdam()) {
                        log.error("failed sidam add roles POST call for userId : " + userId);
                        persistAuditAndThrowIdamException(idamStatusMessage, idamStatus, null);
                    }
                }
                // for success make status = 201
                idamStatus = HttpStatus.CREATED;
                idamStatusMessage = IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus);
                userProfile = persistUserProfileWithAudit(profileData, userId, idamStatusMessage, idamStatus);
            } else {
                log.error("failed sidam GET call for userId : " + userId);
                persistAuditAndThrowIdamException(idamStatusMessage, idamStatus, null);
            }
        } else {
            log.error("Did not get location header");
            idamStatus = HttpStatus.INTERNAL_SERVER_ERROR;
            persistAuditAndThrowIdamException(IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus), idamStatus, null);
        }
        return userProfile;
    }

    private void persistAudit(String message, HttpStatus idamStatus, UserProfile userProfile) {
        Audit audit = new Audit(idamStatus.value(), message, ResponseSource.API, userProfile);
        auditRepository.save(audit);
    }

    private void persistAuditAndThrowIdamException(String message, HttpStatus idamStatus, UserProfile userProfile) {
        persistAudit(message, idamStatus, userProfile);
        throw new IdamServiceException(message, idamStatus);
    }

    public void updateInputRequestWithLatestSidamUserInfo(CreateUserProfileData profileData, IdamRolesInfo idamRolesInfo) {
        profileData.setStatus(IdamStatusResolver.resolveIdamStatus(idamStatusResolverMap, idamRolesInfo));
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

    public Set<String> consolidateRolesFromXuiAndIdam(CreateUserProfileData profileData, IdamRolesInfo idamRolesInfo) {
        Optional<List<String>> roles = Optional.ofNullable(idamRolesInfo.getRoles());
        List<String> idamRoles = roles.isPresent() ? roles.get() : new ArrayList<>();
        List<String> xuiRoles = profileData.getRoles();
        xuiRoles.removeAll(idamRoles);
        return new HashSet<String>(xuiRoles);
    }

    private IdamRolesInfo addIdamRoles(Set<String> rolesToUpdate, String userId) {

        return idamService.addUserRoles(createIdamRolesRequest(rolesToUpdate), userId);
    }

    public Set<Map<String,String>> createIdamRolesRequest(Set<String> rolesToUpdate) {
        Set<Map<String, String>> roles = new HashSet<>();
        rolesToUpdate.forEach(role -> {
            Map<String, String> rolesMap = new HashMap<String, String>();
            rolesMap.put("name", role);
            roles.add(rolesMap);
        });
        return roles;
    }

}
