package uk.gov.hmcts.reform.userprofileapi;

import org.springframework.boot.test.context.TestConfiguration;
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

import static org.mockito.Mockito.mock;

@TestConfiguration
public class UserProfileProviderTestConfiguration {

    @Bean
    @Primary
    public DeleteResourceService<RequestData> resourceDeleter() {
        return mock(DeleteResourceService.class);
    }

    @Bean
    @Primary
    public UserProfileRepository userProfileRepository() {
        return mock(UserProfileRepository.class);
    }

    @Bean
    @Primary
    public IdamService idamService() {
        return mock(IdamService.class);
    }

    @Bean
    @Primary
    public IdamFeignClient idamClient() {
        return mock(IdamFeignClient.class);
    }

    @Bean
    @Primary
    public AuditRepository auditRepository() {
        return mock(AuditRepository.class);
    }

    @Bean
    @Primary
    public AuditService auditService() {
        return mock(AuditService.class);
    }

    @Bean
    @Primary
    public ValidationHelperService validationHelperService() {
        return mock(ValidationHelperService.class);
    }

    @Bean
    @Primary
    public UserProfileQueryProvider querySupplier() {
        return mock(UserProfileQueryProvider.class);
    }

    @Bean
    @Primary
    public ValidationService validationService() {
        return new ValidationServiceImpl();
    }

    @Bean
    @Primary
    public UserProfileCreator getResourceCreator(
            IdamService idamService,
            UserProfileRepository userProfileRepository,
            AuditRepository auditRepository,
            ValidationHelperService validationHelperService,
            IdamFeignClient idamClient) {
        return new UserProfileCreator(
                "secretUri",
                idamService,
                userProfileRepository,
                auditRepository,
                validationHelperService,
                "1",
                "RD_User_Profile_API",
                idamClient);
    }

    @Bean
    @Primary
    public UserProfileRetriever getResourceRetriever(
            AuditService auditService,
            UserProfileQueryProvider querySupplier,
            IdamService idamService,
            AuditRepository auditRepository) {
        return new UserProfileRetriever(auditService, querySupplier, idamService, auditRepository);
    }

    @Bean
    @Primary
    public UserProfileUpdator getResourceUpdater(
            UserProfileRepository userProfileRepository,
            IdamFeignClient idamClient,
            IdamService idamService,
            ValidationService validationService,
            ValidationHelperService validationHelperService,
            AuditService auditService) {
        return new UserProfileUpdator(
                userProfileRepository,
                idamClient,
                idamService,
                validationService,
                validationHelperService,
                auditService);
    }

    @Bean
    @Primary
    @SuppressWarnings("unchecked")
    public UserProfileService<RequestData> userProfileService(
            UserProfileCreator resourceCreator,
            UserProfileRetriever resourceRetriever,
            UserProfileUpdator resourceUpdator,
            DeleteResourceService<RequestData> resourceDeleter,
            UserProfileRepository userProfileRepository) {
        return new UserProfileService<>(
                (ResourceCreator<RequestData>) (ResourceCreator<?>) resourceCreator,
                (ResourceRetriever<RequestData>) (ResourceRetriever<?>) resourceRetriever,
                (ResourceUpdator<RequestData>) (ResourceUpdator<?>) resourceUpdator,
                resourceDeleter,
                userProfileRepository);
    }
}
