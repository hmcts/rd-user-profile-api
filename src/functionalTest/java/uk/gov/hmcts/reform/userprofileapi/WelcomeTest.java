package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import org.springframework.test.context.TestPropertySource;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@TestPropertySource("classpath:application-functional.yaml")
public class WelcomeTest {

    @Value("${targetInstance}") private String targetInstance;

    private static final String MESSAGE = "Welcome to the User Profile API";

    @Test
    public void should_welcome_with_200_response_code() {

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        // TO enable for local testing
        //RestAssured.proxy("proxyout.reform.hmcts.net",8080);
        //SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);

        String response =
                SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(targetInstance)
                .given()
                .when()
                .get("/")
                .then()
                .statusCode(HttpStatus.OK.value())
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .and()
                .extract().body().asString();

        assertThat(response).contains(MESSAGE);
    }
}
