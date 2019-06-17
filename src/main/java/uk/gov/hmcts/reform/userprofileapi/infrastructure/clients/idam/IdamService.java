package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdentityManagerService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

import java.util.ArrayList;
import java.util.List;

@Component
public class IdamService implements IdentityManagerService {

    @Override
    public IdamRegistrationInfo registerUser(CreateUserProfileData requestData) {
        return new IdamRegistrationInfo(HttpStatus.ACCEPTED);
    }

    @Override
    public IdamRolesInfo getIdamRoles() {
        List<String> roles = new ArrayList<String>();
        roles.add("pui-user-manager");
        roles.add("pui-organisation-manager");
        roles.add("pui-finance-manager");
        roles.add("pui-case-manager");
        return new IdamRolesInfo(roles);
    }
}
