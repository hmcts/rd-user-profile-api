package uk.gov.hmcts.reform.userprofileapi.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import feign.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.DeleteResourceService;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Service
@Slf4j
public class DeleteUserProfileServiceImpl implements DeleteResourceService<UserProfileDataRequest> {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private IdamFeignClient idamClient;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    @Transactional
    public UserProfilesDeletionResponse deleteByUserId(String userId) {
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();

        Response idamResponse = idamClient.deleteUser(userId);

        if (idamResponse.status() == NO_CONTENT.value() || idamResponse.status() == NOT_FOUND.value()
                || idamResponse.reason().contains("pending")) {
            Optional<UserProfile> userProfile = userProfileRepository.findByIdamId(userId.trim());

            return validateUserAfterIdamDelete(userProfile, userId, idamResponse.status());

        } else {
            deletionResponse.setMessage("IDAM Delete request failed for userId: " + userId
                    + ". With following IDAM message: " + idamResponse.reason());
            deletionResponse.setStatusCode(idamResponse.status());
            return deletionResponse;
        }
    }

    @Override
    @Transactional
    public UserProfilesDeletionResponse deleteByEmailPattern(String emailPattern) {
        List<String> validUserIdsToDelete = new ArrayList<>();

        List<UserProfile> userProfiles = userProfileRepository.findByEmailIgnoreCaseContaining(emailPattern);

        if (userProfiles.isEmpty()) {
            throw new ResourceNotFoundException("No User Profiles found for email pattern: " + emailPattern);
        }

        userProfiles.forEach(up -> {
            Response idamResponse = idamClient.deleteUser(up.getIdamId());

            if (idamResponse.status() == NO_CONTENT.value() || idamResponse.status() == NOT_FOUND.value()) {
                validUserIdsToDelete.add(up.getIdamId());
            }
        });

        validUserIdsToDelete.forEach(userId -> userProfiles.add(validateUserStatus(userId)));

        return deleteUserProfiles(userProfiles);
    }

    @Override
    @Transactional
    public UserProfilesDeletionResponse delete(UserProfileDataRequest profilesData) {

        List<UserProfile> userProfiles = new ArrayList<>();

        Set<String> userIds = new HashSet<>(profilesData.getUserIds());

        userIds.forEach(userId -> userProfiles.add(validateUserStatus(userId)));

        return deleteUserProfiles(userProfiles);
    }

    /**
     * Only User profile will be deleted from userprofile table but audit table will remain as is.
     */
    public UserProfilesDeletionResponse deleteUserProfiles(List<UserProfile> userProfiles) {

        userProfileRepository.deleteAll(userProfiles);
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        HttpStatus status = NO_CONTENT;
        userProfiles.forEach(userProfile -> {
            UserProfilesDeletionResponse auditResponse = new UserProfilesDeletionResponse();
            auditResponse.setMessage("UserProfile Successfully Deleted::" + userProfile.getIdamId());
            auditResponse.setStatusCode(status.value());
            auditService.persistAudit(auditResponse);
            log.info(loggingComponentName, "Deleted UserProfile Id::" + userProfile.getIdamId());
        });
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(status.value());
        return deletionResponse;
    }

    /**
     * This method is used to find the user profile exist or not with the give userId.
     */
    public UserProfile validateUserStatus(String userId) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId.trim());
        if (userProfileOptional.isEmpty()) {
            throw new ResourceNotFoundException("could not find user profile for userId: " + userId);
        }
        return userProfileOptional.get();
    }

    public UserProfilesDeletionResponse validateUserAfterIdamDelete(
            Optional<UserProfile> userProfile, String userId, int status) {
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();

        if (userProfile.isPresent() && status == BAD_REQUEST.value()) {
            deletionResponse = deleteUserProfiles(singletonList(userProfile.get()));
            deletionResponse.setMessage("User deleted in UP with userId: " + userId
                    + " but not in IDAM due to pending status");
            deletionResponse.setStatusCode(NO_CONTENT.value());
            return deletionResponse;

        } else if (userProfile.isPresent()) {
            return deleteUserProfiles(singletonList(userProfile.get()));

        } else if (status == NO_CONTENT.value()) {
            deletionResponse.setMessage("User deleted in IDAM but was not present in UP with userId: " + userId);

        } else if (status == BAD_REQUEST.value()) {
            deletionResponse.setMessage("User is pending in IDAM and not present in UP with userId: " + userId);
        } else {
            deletionResponse.setMessage("User was not present in IDAM or UP with userId: " + userId);

        }

        deletionResponse.setStatusCode(status);
        return deletionResponse;
    }

}
