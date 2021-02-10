package uk.gov.hmcts.reform.userprofileapi.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        Response idamResponse = idamClient.deleteUser(userId);

        if (idamResponse.status() == NO_CONTENT.value() || idamResponse.status() == NOT_FOUND.value()) {
            UserProfile userProfile = validateUserStatus(userId);
            return deleteUserProfiles(singletonList(userProfile));

        } else {
            UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
            deletionResponse.setMessage("IDAM Delete request failed for userId: " + userId);
            deletionResponse.setStatusCode(idamResponse.status());
            return deletionResponse;
        }
    }

    @Override
    @Transactional
    public UserProfilesDeletionResponse deleteByEmailPattern(String emailPattern) {

        List<UserProfile> userProfiles = userProfileRepository.findByEmailIgnoreCaseContaining(emailPattern);

        for (int i = userProfiles.size() - 1; i >= 0; i--) {
            Response idamResponse = idamClient.deleteUser(userProfiles.get(i).getIdamId());

            if (idamResponse.status() == NO_CONTENT.value() || idamResponse.status() == NOT_FOUND.value()) {
                break;
            } else {
                userProfiles.remove(i);
            }
        }

        Set<String> userIds = userProfiles.stream()
                .map(UserProfile::getIdamId)
                .collect(Collectors.toSet());

        userIds.forEach(userId -> userProfiles.add(validateUserStatus(userId)));

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
            throw new ResourceNotFoundException("could not find user profile for userId:" + userId);
        }
        return userProfileOptional.get();
    }

}

