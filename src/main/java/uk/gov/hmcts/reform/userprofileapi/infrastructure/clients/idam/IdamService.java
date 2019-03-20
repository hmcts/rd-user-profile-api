package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdentityManagerService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@Component
public class IdamService implements IdentityManagerService {

    @Override
    public IdamRegistrationInfo registerUser(CreateUserProfileData requestData) {
        return new IdamRegistrationInfo(HttpStatus.ACCEPTED);
    }
}
