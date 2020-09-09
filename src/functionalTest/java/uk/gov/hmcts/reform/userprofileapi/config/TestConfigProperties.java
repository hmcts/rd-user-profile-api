package uk.gov.hmcts.reform.userprofileapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
public class TestConfigProperties {

    @Value("${idam.auth.clientSecret}")
    public String clientSecret;

    @Value("${idam.api.url}")
    public String idamApiUrl;

    @Value("${idam.auth.tokenAuth}")
    public String tokenAuthorization;

    @Value("${idam.auth.redirectUrl}")
    public String oauthRedirectUrl;

    @Value("${idam.auth.clientId:xuiwebapp}")
    public String clientId;

}
