package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;

import com.google.common.collect.ImmutableList;

import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.List;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@TestPropertySource("classpath:application-functional.yaml")
public class EndpointSecurityTest extends AbstractFunctional {


    private final List<String> endpoints =
        ImmutableList.of("/v1/userprofile/1", "/v1/userprofile");

    @Test
    public void should_allow_unauthenticated_requests_to_welcome_message_and_return_200_response_code() {

        Response response = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .get("/")
                .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        assertThat(response.getBody().asString())
            .contains("Welcome to the User Profile API");
    }

    @Test
    public void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() {

        String response =
            SerenityRest.given()
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

    @Test
    public void should_not_allow_unauthenticated_requests_and_return_401_response_code() {

        endpoints.forEach(callbackEndpoint ->

                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(targetInstance)
                .when()
                .get(callbackEndpoint)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
        );
    }

    @Test
    public void should_not_allow_requests_without_valid_service_authorisation_and_return_401_response_code() {

        String invalidServiceToken = "invalid";

        endpoints.forEach(endpoint ->

                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(targetInstance)
                .header("ServiceAuthorization", invalidServiceToken)
                .when()
                .get(endpoint)
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
        );
    }

}
