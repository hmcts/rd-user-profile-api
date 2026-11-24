package uk.gov.hmcts.reform.userprofileapi;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.DeleteResourceService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceCreator;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileCreator;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileUpdator;
import uk.gov.hmcts.reform.userprofileapi.service.impl.ValidationServiceImpl;

@TestConfiguration
public class UserProfileProviderTestConfiguration {


    private ResourceCreator<RequestData> resourceCreator;

    private ResourceRetriever<RequestData> resourceRetriever;

    private ResourceUpdator<RequestData> resourceUpdator;

    @MockBean
    private DeleteResourceService<RequestData> resourceDeleter;

    @MockBean
    private UserProfileRepository userProfileRepository;

    @MockBean
    private IdamService idamService;

    @MockBean
    private IdamFeignClient idamClient;

    @MockBean
    private AuditRepository auditRepository;

    @MockBean
    private AuditService auditService;

    @MockBean
    private ValidationHelperService validationHelperService;

    private ValidationService validationService;

    @MockBean
    private UserProfileQueryProvider querySupplier;


    @Bean
    @Primary
    public UserProfileService<RequestData> userProfileService() {
        return new UserProfileService<>(resourceCreator, resourceRetriever,
                resourceUpdator, resourceDeleter, userProfileRepository);
    }

    @Bean
    @Primary
    public UserProfileCreator getResourceCreator() {
        return new UserProfileCreator("secretUri",idamService,userProfileRepository,
                auditRepository,validationHelperService,"1","RD_User_Profile_API",
                idamClient);
    }


    @Bean
    @Primary
    public UserProfileRetriever getResourceRetriever() {
        return new UserProfileRetriever(auditService,querySupplier,idamService,auditRepository);
    }

    @Bean
    @Primary
    public UserProfileUpdator getResourceUpdater() {
        return new UserProfileUpdator(userProfileRepository,idamClient,idamService,validationService,
                validationHelperService,auditService);
    }

    @Bean
    @Primary
    public ValidationService getValidationService() {
        return new ValidationServiceImpl();
    }

}
