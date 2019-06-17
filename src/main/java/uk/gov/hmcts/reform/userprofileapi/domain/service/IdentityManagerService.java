package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@Component
public interface IdentityManagerService {

    IdamRegistrationInfo registerUser(CreateUserProfileData requestData);

    IdamRolesInfo getIdamRoles();
}