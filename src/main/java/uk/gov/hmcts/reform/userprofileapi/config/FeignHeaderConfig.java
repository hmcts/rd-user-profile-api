package uk.gov.hmcts.reform.userprofileapi.config;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "feign.allow")
@Getter
@AllArgsConstructor
public class FeignHeaderConfig {
    private final List<String> headers;
}

