package uk.gov.hmcts.reform.userprofileapi.integration.security;

import io.restassured.specification.RequestSpecification;
import net.serenitybdd.rest.SerenityRest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.integration.AuthorizationEnabledIntegrationTest;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import static uk.gov.hmcts.reform.userprofileapi.domain.enums.UserProfileField.USERCATEGORY;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.UserProfileField.USERTYPE;

public class BaseSecurityIntegrationTest extends AuthorizationEnabledIntegrationTest {

    protected static final String VALID_ISSUER_1 = "http://localhost:5062/o";
    protected static final String VALID_ISSUER_2 = "https://secondary-idam.platform.hmcts.net";
    protected static final String ROGUE_ISSUER = "https://rogue-issuer.com";

    protected static final String CREATE_USER_URL = "/v1/userprofile";
    public static final String UP_SERVICE_NAME = "rd-user-profile-api";

    protected RequestSpecification jwtRequest(
            String issuer,
            boolean expired) {

        return SerenityRest.given()
            .baseUri(testApplicationServer.getBaseUrl())
            .headers(getHttpHeaders(issuer, expired, null, "prd-admin"));
    }

    protected RequestSpecification unexpiredJwt(
            String issuer) {

        return jwtRequest(issuer, false);
    }

    protected RequestSpecification expiredJwt(
            String issuer) {

        return jwtRequest(issuer, true);
    }

    protected UserProfileCreationData getUserProfileCreationData() {
        UserProfileCreationData result = new UserProfileCreationData();
        result.setResendInvite(false);
        result.setUserType(USERTYPE.name());
        result.setUserCategory(USERCATEGORY.name());
        return result;
    }

    protected UserProfileCreationResponse getUserProfileCreationResponse() {
        return new UserProfileCreationResponse();
    }
}