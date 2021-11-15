package uk.gov.hmcts.reform.userprofileapi.integration.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.hmcts.reform.userprofileapi.domain.DateProvider;

import static org.mockito.Mockito.mock;

@Configuration
public class TestConfig {

    @Bean
    @Primary
    public DateProvider dateProvider() {
        return mock(DateProvider.class);
    }
}
