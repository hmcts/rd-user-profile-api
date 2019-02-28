package uk.gov.hmcts.reform.userprofileapi.integration.util;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.userprofileapi.domain.DateProvider;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public DateProvider dateProvider() {
        return mock(DateProvider.class);
    }
}
