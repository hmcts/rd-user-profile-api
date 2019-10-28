package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.http.HttpStatus;

@Slf4j
public class SmokeTest {

    private final String targetInstance =
            StringUtils.defaultIfBlank(
                    System.getenv("TEST_URL"),
                    "http://localhost:8091"
            );

    @Test
    public void should_prove_app_is_running_and_healthy() {

        log.info("Smoke test executing on " + targetInstance);

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response = SerenityRest
            .given()
            .relaxedHTTPSValidation()
            .baseUri(targetInstance)
            .when()
            .get("/health")
            .then()
            .statusCode(HttpStatus.OK.value())
            .and()
            .extract().body().asString();

        assertThat(response)
                .contains("UP");
    }
}