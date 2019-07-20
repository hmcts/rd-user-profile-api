package uk.gov.hmcts.reform.userprofileapi.service;

import java.net.URI;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
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

    public UserProfile create(CreateUserProfileData profileData) {

        UUID userId = UUID.randomUUID();
        profileData.setId(userId.toString());
        final IdamRegistrationInfo idamRegistrationInfo = idamService.registerUser(profileData);
        HttpStatus idamStatus = idamRegistrationInfo.getIdamRegistrationResponse();
        if (idamRegistrationInfo.isSuccessFromIdam()) {
            return persistUserProfileWithAudit(profileData, idamRegistrationInfo.getStatusMessage(), idamRegistrationInfo.getIdamRegistrationResponse());
        } else if (idamRegistrationInfo.isDuplicateUser()) {
            log.info("User already exist in sidam for eamil : " + profileData.getEmail());
            return handleDuplicateUser(profileData, idamRegistrationInfo);
        } else {
            persistAudit(idamRegistrationInfo.getStatusMessage(), idamStatus, null);
            throw new IdamServiceException(idamRegistrationInfo.getStatusMessage(), idamStatus);
        }
    }

    private UserProfile persistUserProfileWithAudit(CreateUserProfileData profileData, String stausMessage, HttpStatus idamStatus) {
        UserProfile userProfile = null;
        if (idamStatus.is2xxSuccessful()) {
            userProfile = new UserProfile(profileData, idamStatus);
            userProfile.setIdamId(UUID.fromString(profileData.getId()));
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
        ResponseEntity responseEntity = idamRegistrationInfo.getResponse();

        if (responseEntity != null && responseEntity.getHeaders() != null) {
            //get userId from location header
            userIdUri = idamRegistrationInfo.getResponse().getHeaders().getLocation();
            userId = userIdUri != null ? userIdUri.toString().substring(sidamGetUri.length()) : null;
            log.error("Received existing idam userId : " + userId);
            // search with id to get roles
            IdamRolesInfo idamRolesInfo = idamService.fetchUserById(userId);
            idamStatus = idamRolesInfo.getResponseStatusCode();
            idamStatusMessage = idamRolesInfo.getStatusMessage();

            if (idamRolesInfo.isSuccessFromIdam()) {
                // set updated user info
                updateInputRequestWithLatestSidamUserInfo(profileData, idamRolesInfo, userId);

                //consolidate XUI + SIDAM roles having unique roles
                List<String> rolesToUpdate = rolesToUpdate = consolidateRolesFromXuiAndIdam(profileData, idamRolesInfo);

                //update roles in Idam
                idamRolesInfo = updateIdamRoles(rolesToUpdate, userId);

                idamStatus = idamRolesInfo.getResponseStatusCode();
                idamStatusMessage = idamRolesInfo.getStatusMessage();
                if (!idamRolesInfo.isSuccessFromIdam()) {
                    log.error("failed sidam PUT call for userId : " + userId);
                    persistAuditAndThrowIdamException(idamStatusMessage, idamStatus, null);
                }
                // for success make status = 201
                idamStatus = HttpStatus.CREATED;
                idamStatusMessage = IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus);
                userProfile = persistUserProfileWithAudit(profileData, idamStatusMessage, idamStatus);
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

    private void updateInputRequestWithLatestSidamUserInfo(CreateUserProfileData profileData, IdamRolesInfo idamRolesInfo, String userId) {
        profileData.setId(userId);
        profileData.setEmail(idamRolesInfo.getEmail());
        profileData.setFirstName(idamRolesInfo.getForename());
        profileData.setLastName(idamRolesInfo.getSurname());
    }

    private List<String> consolidateRolesFromXuiAndIdam(CreateUserProfileData profileData, IdamRolesInfo idamRolesInfo) {
        Optional<List<String>> roles = Optional.ofNullable(idamRolesInfo.getRoles());
        List<String> rolesToUpdate = roles.isPresent() ? roles.get() : new ArrayList<>();
        rolesToUpdate.addAll(profileData.getRoles());
        Set<String> rolesSet = new HashSet<String>(rolesToUpdate);
        return new ArrayList<String>(rolesSet);
    }

    private IdamRolesInfo updateIdamRoles(List<String> rolesToUpdate, String userId) {
        List<Map<String,String>> roles = new ArrayList<>();
        rolesToUpdate.forEach(role -> {
            Map<String, String> rolesMap = new HashMap<String, String>();
            rolesMap.put("name", role);
            roles.add(rolesMap);
        });
        return idamService.updateUserRoles(roles, userId);
    }

}
