package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileCreator implements ResourceCreator<CreateUserProfileData> {

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;

    public UserProfile create(CreateUserProfileData profileData) {

        final IdamRegistrationInfo idamRegistrationInfo = idamService.registerUser(profileData);
        HttpStatus idamStatus = idamRegistrationInfo.getIdamRegistrationResponse();
        if (HttpStatus.CREATED == idamStatus) {
            UserProfile userProfile = new UserProfile(profileData, idamRegistrationInfo);
            userProfile = userProfileRepository.save(userProfile);
            persistAudit(idamRegistrationInfo, idamStatus, userProfile);
            return userProfile;
        } else {
            persistAudit(idamRegistrationInfo, idamStatus, null);
            throw new IdamServiceException("Idam registration failed", idamStatus);
        }
    }

    private void persistAudit(IdamRegistrationInfo idamRegistrationInfo, HttpStatus idamStatus, UserProfile userProfile) {
        Audit audit = new Audit(idamStatus.value(), idamRegistrationInfo.getStatusMessage(), ResponseSource.SIDAM, userProfile);
        auditRepository.save(audit);
    }

}
