package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.List;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;

@Component
public interface IdentityManagerService {

    IdamRegistrationInfo registerUser(CreateUserProfileData requestData);

    IdamRolesInfo getUserById(String id);

    IdamRolesInfo searchUserByEmail(String email);

    IdamRolesInfo updateUserRoles(List roleRequest, String userId);

}