package uk.gov.hmcts.reform.userprofileapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

@TestConfiguration
public class UserProfileProviderTestConfiguration {

    @Bean
    @Primary
    public UserProfileService<RequestData> getUserProfileService() {
        return new UserProfileService<>();
    }

    @MockBean
    private IdamService idamService;

    @MockBean
    private ValidationService validationService;

}
