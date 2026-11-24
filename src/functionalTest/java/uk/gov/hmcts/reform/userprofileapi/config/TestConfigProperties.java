package uk.gov.hmcts.reform.userprofileapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
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

    @Value("${targetInstance}")
    protected String targetInstance;

    @Value("${idam.s2s-auth.totp_secret}")
    protected String s2sSecret;

    @Value("${idam.s2s-auth.url}")
    protected String s2sBaseUrl;

    @Value("${idam.s2s-auth.microservice:rd_user_profile_api}")
    protected String s2sMicroservice;

    @Value("${exui.role.hmcts-admin}")
    protected String hmctsAdmin;

    @Value("${exui.role.pui-user-manager}")
    protected String puiUserManager;

    @Value("${exui.role.pui-organisation-manager}")
    protected String puiOrgManager;

    @Value("${exui.role.pui-finance-manager}")
    protected String puiFinanceManager;

    @Value("${exui.role.pui-case-manager}")
    protected String puiCaseManager;

    @Value("${resendInterval}")
    protected String resendInterval;


}
