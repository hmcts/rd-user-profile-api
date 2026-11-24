package uk.gov.hmcts.reform.userprofileapi.integration.security;

import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.reform.userprofileapi.integration.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

public class BaseSecurityIntegrationTest extends AuthorizationEnabledIntegrationTest {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";
    protected static final String CREATE_USER_URL = "/v1/userprofile";
    protected static final UserProfileCreationData USERP_ROFILE_CREATION_DATA = buildCreateUserProfileData();

    protected RequestSpecification jwtRequest(
            String issuer,
            boolean expired) {
        return SerenityRest.given()
            .baseUri(testApplicationServer.getBaseUrl())
            .headers(getHttpHeaders(issuer, expired));
    }

    protected RequestSpecification unexpiredJwt(
            String issuer) {
        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(
            String issuer) {
        return jwtRequest(issuer, true);
    }


}