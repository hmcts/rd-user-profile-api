package uk.gov.hmcts.reform.userprofileapi.domain.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RoleName;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RoleRequest;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice.ErrorConstants;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@Service
@Slf4j
public class UserProfileCreator implements ResourceCreator<CreateUserProfileData> {

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

        if (idamRegistrationInfo.getResponse().getHeaders() != null) {
            //get userId from location header
            userIdUri = idamRegistrationInfo.getResponse().getHeaders().getLocation();
            userId = userIdUri != null ? userIdUri.toString() : null;
            log.error("Received existing idam userId : " + userId);
            // search with id to get roles
            IdamRolesInfo idamRolesInfo = idamService.getUserById(userId);
            idamStatus = idamRolesInfo.getIdamGetResponseStatusCode();
            idamStatusMessage = idamRolesInfo.getStatusMessage();

            if (idamRolesInfo.isSuccessFromIdam()) {
                // set updated user info
                updateInputRequestWithLatestSidamUserInfo(profileData, idamRolesInfo, userId);

                //consolidate XUI + SIDAM roles having unique roles
                List<String> rolesToUpdate = rolesToUpdate = consolidateRolesFromXuiAndIdam(profileData, idamRolesInfo);

                //update roles in Idam
                idamRolesInfo = updateIdamRoles(rolesToUpdate, userId);

                idamStatus = idamRolesInfo.getIdamGetResponseStatusCode();
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
        Audit audit = new Audit(idamStatus.value(), message, ResponseSource.SIDAM, userProfile);
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
        List<String> rolesToUpdate = idamRolesInfo.getRoles();
        rolesToUpdate.addAll(profileData.getRoles());
        Set<String> rolesSet = new HashSet<String>(rolesToUpdate);
        return new ArrayList<String>(rolesSet);
    }

    private IdamRolesInfo updateIdamRoles(List<String> rolesToUpdate, String userId) {
        RoleRequest roleRequest = new RoleRequest();
        List<RoleName> roles = rolesToUpdate.stream().map(role ->
            new RoleName(role)).collect(Collectors.toList());
        roleRequest.setRoles(roles);
        return idamService.updateUserRoles(roleRequest, userId);
    }

}
