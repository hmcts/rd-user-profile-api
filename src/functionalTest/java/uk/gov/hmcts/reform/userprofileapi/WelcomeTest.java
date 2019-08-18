package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.TestPropertySource;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
@TestPropertySource("classpath:application-functional.yaml")
public class WelcomeTest {

    @Value("${targetInstance}") private String targetInstance;

    private static final String MESSAGE = "Welcome to the User Profile API";

    @Test
    public void should_welcome_with_200_response_code() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();

        String response =
                SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(targetInstance)
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .and()
                .extract().body().asString();

        assertThat(response).contains(MESSAGE);
    }
}
