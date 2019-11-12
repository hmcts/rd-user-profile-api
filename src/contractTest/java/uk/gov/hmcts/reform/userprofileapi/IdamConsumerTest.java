package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.PactDslJsonArray;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.RestAssured;
import io.restassured.config.EncoderConfig;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import java.util.Map;
import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(PactConsumerTestExt.class)
@ExtendWith(SpringExtension.class)
public class IdamConsumerTest {

    private static final String IDAM_DETAILS_URL = "/details";
    private static final String IDAM_GET_USER_URL = "/api/v1/users/.*";
    private static final String IDAM_POST_USER_REGISTRATION_URL = "/api/v1/users/registration";
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiRm8rQXAybThDT3ROb290ZjF4TWg0bGc3MFlBPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJmcmVnLXRlc3QtdXNlci1ZOHlqVURSeWpyQGZlZW1haWwuY29tIiwiYXV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYTU2MDliYjYtYzEzYi00MjQ0LTg3ODItNDNmZGViMDZlMDBjIiwiaXNzIjoiaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2VydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1hYXQuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiYWNjNmUyYTAtMWExYi00OGM3LWJmZGItNzI1NjllM2E1NjkzIiwiYXVkIjoicmQtcHJvZmVzc2lvbmFsLWFwaSIsIm5iZiI6MTU2OTQ0MTkxMSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImNyZWF0ZS11c2VyIiwibWFuYWdlLXVzZXIiXSwiYXV0aF90aW1lIjoxNTY5NDQxOTExMDAwLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU2OTQ1NjMxMSwiaWF0IjoxNTY5NDQxOTExLCJleHBpcmVzX2luIjoxNDQwMCwianRpIjoiY2Q5MWM0NjQtMzU0Zi00N2I2LTkwYTUtNWY2Y2U3NGUwYTY5In0.aLobAYYCxkmryzKV1stmag63h-ndxrDjO4462YERcLDIXVmvFJNXfdPRg9U8WGv0GkOrSkHVJ7tbdLQySnOVYulXkPl71g5MqU7ZuEQvHaBpfW9exBCfP-pw8kWyMUck-rB00tkEX7ZpS6euQM0WVbdczPnClxR3tWwktPfN-bCo6PPwqiMkC1DgTmjQBMtjgP1nEiJM7Kocqb2X3OCItf4lps1_nSG68jI98fwaLn8WQgk1sw9eebskChXDfpmIyreeGFWpHNpdFqOFfYEC5FnSgXHQw7Eu-hc5RofPZzKFrbwZHC31t5guK9Wq8zn9Xwe6743g4ozm3EHN8fsjVQ"; //todo

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8", "UTF-8"));
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executeGetUserDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user details")
                .uponReceiving("Provider receives a GET /details request from an RD - USER PROFILE API")
                .path(IDAM_DETAILS_URL)
                .method(HttpMethod.GET.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserDetailsResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserDetailsAndGet200")
    public void should_get_user_details_with_access_token(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .when()
                        .get(mockServer.getUrl() + IDAM_DETAILS_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .body()
                        .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("id")).isNotBlank();
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executeGetUserAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user")
                .uponReceiving("Provider receives a GET /api/v1/users/.* request from an RD - USER PROFILE API")
                .path(IDAM_GET_USER_URL)
                .method(HttpMethod.GET.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserAndGet200")
    public void should_get_user_from_elastic_search(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .when()
                        .get(mockServer.getUrl() + IDAM_GET_USER_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .body()
                        .asString();

        JSONObject response = new JSONObject(actualResponseBody);

        assertThat(actualResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("forename")).isNotBlank();
        assertThat(response.getString("surname")).isNotBlank();

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    private PactDslJsonBody createUserDetailsResponse() {
        boolean status = true;
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager")
                .string("pui-case-manager");

        return new PactDslJsonBody()
                .stringType("id", "a833c2e2-2c73-4900-96ca-74b1efb37928")
                .stringType("forename", "Seymore")
                .stringType("surname", "Skinner")
                .stringType("email", "leeroy@jenkins.com")
                .booleanType("accountStatus", true)
                .object("roles", array);
    }

    private PactDslJsonBody createUserResponse() {
        boolean status = true;
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager")
                .string("pui-case-manager");

        return new PactDslJsonBody()
                .booleanType("active", true)
                .stringType("forename", "Jack")
                .stringType("surname", "Skellington")
                .stringType("email", "jack@spookmail.com")
                .booleanType("pending", false)
                .object("roles", array);
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executePostRegistrationAndGet201(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns 201 Created status")
                .uponReceiving("Provider receives a POST /registration request from an RD - USER PROFILE API")
                .path(IDAM_POST_USER_REGISTRATION_URL)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.CREATED.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePostRegistrationAndGet201")
    public void should_post_for_registration_and_return_201(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        Response actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                        .when()
                        .post(mockServer.getUrl() + IDAM_POST_USER_REGISTRATION_URL)
                        .then()
                        .statusCode(201)
                        .and()
                        .extract()
                        .response();

        assertThat(actualResponseBody.getStatusCode()).isEqualTo(201);
    }
}