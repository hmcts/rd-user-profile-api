package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SpringIntegrationSerenityRunner.class)
public class SmokeTest {

    private final String targetInstance = "http://rd-user-profile-api-aat.service.core-compute-aat.internal";
    /* StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:8090"
    );*/

    @Test
    public void should_prove_app_is_running_and_healthy() {
        // local test
        SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
        RestAssured.proxy("proxyout.reform.hmcts.net", 8080);

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        Response response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .get("/")
                .andReturn();

        if (null != response.body()) {
            assertThat(response.body().asString())
                    .contains("Welcome to the User Profile API");

        }

    }
}
