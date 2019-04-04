package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@Component
public interface IdentityManagerService {

    String registerUser(CreateUserProfileData requestData);

}
