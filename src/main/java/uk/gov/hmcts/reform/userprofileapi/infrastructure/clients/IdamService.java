package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import java.util.UUID;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdentityManagerService;

@Component
public class IdamService implements IdentityManagerService {

    @Override
    public String registerUser(CreateUserProfileData requestData) {
        //TODO Not implemented yet
        return UUID.randomUUID().toString();
    }
}
