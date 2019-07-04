package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RoleRequest;


@Component
public interface IdentityManagerService {

    IdamRegistrationInfo registerUser(CreateUserProfileData requestData);

    IdamRolesInfo getUserById(String id);

    IdamRolesInfo searchUserByEmail(String email);

    IdamRolesInfo updateUserRoles(RoleRequest roleRequest, String userId);

}