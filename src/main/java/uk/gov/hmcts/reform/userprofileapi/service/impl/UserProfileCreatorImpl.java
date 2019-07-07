package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.controllers.advice.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceCreator;

@Service
public class UserProfileCreatorImpl implements ResourceCreator<CreateUserProfileData> {

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;

    public UserProfile create(CreateUserProfileData profileData) {

        final IdamRegistrationInfo idamRegistrationInfo = idamService.registerUser(profileData);
        HttpStatus idamStatus = idamRegistrationInfo.getIdamRegistrationResponse();
        if (idamStatus.is2xxSuccessful()) {
            UserProfile userProfile = new UserProfile(profileData, idamRegistrationInfo);
            userProfile = userProfileRepository.save(userProfile);
            persistAudit(idamRegistrationInfo, idamStatus, userProfile);
            return userProfile;
        } else {
            persistAudit(idamRegistrationInfo, idamStatus, null);
            throw new IdamServiceException(idamRegistrationInfo.getStatusMessage(), idamStatus);
        }
    }

    private void persistAudit(IdamRegistrationInfo idamRegistrationInfo, HttpStatus idamStatus, UserProfile userProfile) {
        Audit audit = new Audit(idamStatus.value(), idamRegistrationInfo.getStatusMessage(), ResponseSource.SIDAM, userProfile);
        auditRepository.save(audit);
    }

}
