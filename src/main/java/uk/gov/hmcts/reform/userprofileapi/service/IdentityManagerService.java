package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Component
public interface IdentityManagerService {

    IdamRegistrationInfo registerUser(CreateUserProfileData requestData);

    IdamRolesInfo getUserById(UserProfile userProfile);

}