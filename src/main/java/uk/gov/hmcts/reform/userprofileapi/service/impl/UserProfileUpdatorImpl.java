package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isSameAsExistingUserProfile;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUpdateUserProfileRequestValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.clients.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.controllers.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;

@Service
public class UserProfileUpdatorImpl implements ResourceUpdator<UpdateUserProfileData> {

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;

    @Override
    public UserProfile update(UpdateUserProfileData updateUserProfileData, ResourceRetriever resourceRetriever, String userId) {

        HttpStatus status = HttpStatus.OK;
        UserProfile userProfile = null;
        if (!isUserIdValid(userId, false)) {
            persistAudit(HttpStatus.NOT_FOUND, null);
            throw new ResourceNotFoundException("userId provided is malformed");
        }

        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(java.util.UUID.fromString(userId));
        userProfile =  userProfileOptional.orElse(null);

        if (userProfile == null) {
            persistAudit(HttpStatus.NOT_FOUND, null);
            throw new ResourceNotFoundException("could not find user profile for userId: " + userId);
        } else if (!isUpdateUserProfileRequestValid(updateUserProfileData)) {
            persistAudit(HttpStatus.BAD_REQUEST, null);
            throw new RequiredFieldMissingException("Update user profile request is not valid for userId: " + userId);
        } else if (!isSameAsExistingUserProfile(updateUserProfileData, userProfile)) {
            userProfile.setEmail(updateUserProfileData.getEmail().trim());
            userProfile.setFirstName(updateUserProfileData.getFirstName().trim());
            userProfile.setLastName(updateUserProfileData.getLastName().trim());
            userProfile.setStatus(IdamStatus.valueOf(updateUserProfileData.getIdamStatus()));
            try {
                userProfile = userProfileRepository.save(userProfile);
            } catch (Exception ex) {
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }
        persistAudit(status, userProfile);
        return userProfile;
    }

    private void persistAudit(HttpStatus idamStatus, UserProfile userProfile) {
        Audit audit = new Audit(idamStatus.value(), resolveStatusAndReturnMessage(idamStatus), ResponseSource.QUARTZ, userProfile);
        auditRepository.save(audit);
    }
}
