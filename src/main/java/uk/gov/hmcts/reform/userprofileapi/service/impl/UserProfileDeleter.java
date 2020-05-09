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
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfilesDeletionData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceDeleter;

@Service
@Slf4j
public class UserProfileDeleter implements ResourceDeleter<UserProfilesDeletionData> {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private AuditService auditService;

    @Override
    @Transactional
    public UserProfilesDeletionResponse delete(UserProfilesDeletionData profilesData) {

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

        List<Audit> deleteAuditRecords = new ArrayList<Audit>();
        userProfiles.forEach(userProfile -> deleteAuditRecords.addAll(userProfile.getResponses()));
        auditRepository.deleteAll(deleteAuditRecords);
        userProfileRepository.deleteAll(userProfiles);
        UserProfilesDeletionResponse deletionResponse = new UserProfilesDeletionResponse();
        HttpStatus status = HttpStatus.NO_CONTENT;
        deletionResponse.setMessage("UserProfiles Successfully Deleted");
        deletionResponse.setStatusCode(status.value());
        auditService.persistAudit(deletionResponse);
        return deletionResponse;

    }

    /**
     * This method is used to find the user profile exist or not with the give idamId.
     *
     */
    private UserProfile validateUserStatus(String idamId) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(idamId);
        if (!userProfileOptional.isPresent()) {
            throw new ResourceNotFoundException("could not find user profile for userId:" + idamId);
        }
        return userProfileOptional.get();
    }

}

