package uk.gov.hmcts.reform.userprofileapi;

import feign.okhttp.OkHttpClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

@TestConfiguration
@ComponentScan(basePackages = "uk.gov.hmcts.reform.userprofileapi")
public class ProfileConfig {

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}
