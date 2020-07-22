package uk.gov.hmcts.reform.userprofileapi.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.DeleteResourceService;

@Service
@Slf4j
public class DeleteUserProfileServiceImpl implements DeleteResourceService<UserProfileDataRequest> {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional
    public UserProfilesDeletionResponse delete(UserProfileDataRequest profilesData) {

        List<UserProfile> userProfiles = new ArrayList<UserProfile>();
        Set<String> userIds = new HashSet<String>(profilesData.getUserIds());
        userIds.forEach(userId -> userProfiles.add(validateUserStatus(userId)));

        return deleteUserProfiles(userProfiles);
    }

    /**
     * Either delete all the audit and userProfiles data from the data base or none.
     *
     */
    private UserProfilesDeletionResponse deleteUserProfiles(List<UserProfile> userProfiles) {

        userProfileRepository.deleteAll(userProfiles);
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        HttpStatus status = HttpStatus.NO_CONTENT;
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(status.value());
        auditService.persistAudit(deletionResponse);
        return deletionResponse;

    }

    /**
     * This method is used to find the user profile exist or not with the give userId.
     *
     */
    private UserProfile validateUserStatus(String userId) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId.trim());
        if (!userProfileOptional.isPresent()) {
            throw new ResourceNotFoundException("could not find user profile for userId:" + userId);
        }
        return userProfileOptional.get();
    }

}

