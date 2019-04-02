package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam;

import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdentityManagerService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@Component
public class IdamService implements IdentityManagerService {

    @Override
    public String registerUser(CreateUserProfileData requestData) {
        return UUID.randomUUID().toString();
    }
}
