package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isSameAsExistingUserProfile;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileUpdator implements ResourceUpdator<UpdateUserProfileData> {

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;

    @Override
    public UserProfile update(UpdateUserProfileData updateUserProfileData, ResourceRetriever resourceRetriever, String userId) {

        HttpStatus status = HttpStatus.OK;
        UserProfile userProfile = (userProfileRepository.findByIdamId(java.util.UUID.fromString(userId))).orElseGet(null);
        if(userProfile == null) {
            status = HttpStatus.NOT_FOUND;
            persistAudit(status, null);
            throw new ResourceNotFoundException("could not find resource from database with given identifier: " + userId);
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
        Audit audit = new Audit(idamStatus.value(), idamStatus.getReasonPhrase(), ResponseSource.QUARTZ, userProfile);
        auditRepository.save(audit);
    }
}
