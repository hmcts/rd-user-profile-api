package uk.gov.hmcts.reform.userprofileapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

@Configuration
@Lazy
public class ServiceTokenGeneratorConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceTokenGeneratorConfiguration.class);

    @Bean
    public AuthTokenGenerator authTokenGenerator(
            @Value("${idam.s2s-auth.totp_secret}") String secret,
            @Value("${idam.s2s-auth.microservice}") String microService,
            ServiceAuthorisationApi serviceAuthorisationApi
    ) {

        LOG.info(String.format("::totpSecret in ServiceTokenGeneratorConfiguration:: %S", secret));
        LOG.info(String.format("::s2sAuthMicroservicename in ServiceTokenGeneratorConfiguration:: %S", microService));

        return AuthTokenGeneratorFactory.createDefaultGenerator(
            secret,
            microService,
            serviceAuthorisationApi
        );
    }
}
