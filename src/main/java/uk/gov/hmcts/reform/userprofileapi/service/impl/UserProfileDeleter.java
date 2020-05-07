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
    @Transactional
    private UserProfilesDeletionResponse deleteUserProfiles(List<UserProfile> userProfiles) {
        UserProfilesDeletionResponse attributeResponse = new UserProfilesDeletionResponse();
        List<Audit> deleteAuditRecords = new ArrayList<Audit>();
        HttpStatus status = HttpStatus.NO_CONTENT;
        try {
            userProfiles.forEach(userProfile -> deleteAuditRecords.addAll(userProfile.getResponses()));
            auditRepository.deleteAll(deleteAuditRecords);
            userProfileRepository.deleteAll(userProfiles);
            attributeResponse.setMessage("UserProfiles Successfully Deleted");
            attributeResponse.setStatusCode(204);

        } catch (Exception ex) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            attributeResponse.setMessage("While deleting userProfiles facing some issues");
            attributeResponse.setStatusCode(500);
            attributeResponse.setErrorDescription(ex.getMessage());
        }
        auditService.persistAudit(attributeResponse);
        return attributeResponse;

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

