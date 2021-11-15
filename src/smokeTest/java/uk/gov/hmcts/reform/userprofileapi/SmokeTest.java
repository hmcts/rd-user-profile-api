package uk.gov.hmcts.reform.userprofileapi;

import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

class SmokeTest {

    // to test locally please use the line in private final string targetInstance
    // private final String targetInstance = "http://rd-user-profile-api-aat.service.core-compute-aat.internal";
    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:8091");

    @Test
    void should_prove_app_is_running_and_healthy() {
        // local test
        /*SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);*/

        SerenityRest.useRelaxedHTTPSValidation();


        Response response = SerenityRest
                .given().log().all()
                .baseUri(targetInstance)
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .get("/")
                .andReturn();

        if (null != response && response.statusCode() == 200) {
            assertThat(response.body().asString())
                    .contains("Welcome to the User Profile API");
        } else {
            Assertions.fail();
        }
    }
}
