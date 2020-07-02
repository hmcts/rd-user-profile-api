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

    private static final String EMAIL = "seymore@skinner.com";
    private static final String ID = "a833c2e2-2c73-4900-96ca-74b1efb37928";

    private static final String IDAM_POST_USER_REGISTRATION_URL = "/api/v1/users/registration";
    private static final String IDAM_USER_BY_ID_URL = "/api/v1/users/" + ID;
    private static final String IDAM_USER_ROLES_BY_ID_URL = "/api/v1/users/" + ID + "/roles";
    private static final String ACCESS_TOKEN = "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiRm8rQXAybThDT3ROb290ZjF"
            + "4TWg0bGc3MFlBPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJmcmVnLXRlc3QtdXNlci1ZOHlqVURSeWpyQGZlZW1haWwuY29tIiwiY"
            + "XV0aF9sZXZlbCI6MCwiYXVkaXRUcmFja2luZ0lkIjoiYTU2MDliYjYtYzEzYi00MjQ0LTg3ODItNDNmZGViMDZlMDBjIiwiaXNzIjo"
            + "iaHR0cHM6Ly9mb3JnZXJvY2stYW0uc2VydmljZS5jb3JlLWNvbXB1dGUtaWRhbS1hYXQuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1d"
            + "GgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoiYWN"
            + "jNmUyYTAtMWExYi00OGM3LWJmZGItNzI1NjllM2E1NjkzIiwiYXVkIjoicmQtcHJvZmVzc2lvbmFsLWFwaSIsIm5iZiI6MTU2OTQ0M"
            + "TkxMSwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInNjb3BlIjpbIm9wZW5pZCIsInByb2ZpbGUiLCJyb2xlcyIsImN"
            + "yZWF0ZS11c2VyIiwibWFuYWdlLXVzZXIiXSwiYXV0aF90aW1lIjoxNTY5NDQxOTExMDAwLCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6M"
            + "TU2OTQ1NjMxMSwiaWF0IjoxNTY5NDQxOTExLCJleHBpcmVzX2luIjoxNDQwMCwianRpIjoiY2Q5MWM0NjQtMzU0Zi00N2I2LTkwYTU"
            + "tNWY2Y2U3NGUwYTY5In0.aLobAYYCxkmryzKV1stmag63h-ndxrDjO4462YERcLDIXVmvFJNXfdPRg9U8WGv0GkOrSkHVJ7tbdLQyS"
            + "nOVYulXkPl71g5MqU7ZuEQvHaBpfW9exBCfP-pw8kWyMUck-rB00tkEX7ZpS6euQM0WVbdczPnClxR3tWwktPfN-bCo6PPwqiMkC1D"
            + "gTmjQBMtjgP1nEiJM7Kocqb2X3OCItf4lps1_nSG68jI98fwaLn8WQgk1sw9eebskChXDfpmIyreeGFWpHNpdFqOFfYEC5FnSgXHQw"
            + "7Eu-hc5RofPZzKFrbwZHC31t5guK9Wq8zn9Xwe6743g4ozm3EHN8fsjVQ";

    @BeforeEach
    public void setUp() {
        RestAssured.defaultParser = Parser.JSON;
        RestAssured.config().encoderConfig(new EncoderConfig("UTF-8",
                "UTF-8"));
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executePostRegistrationAndGet201(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns 201 Created status")
                .uponReceiving("a POST /registration request from an RD - "
                        + "USER PROFILE API")
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
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .post(mockServer.getUrl() + IDAM_POST_USER_REGISTRATION_URL)
                        .then()
                        .statusCode(201)
                        .and()
                        .extract()
                        .response();

        assertThat(actualResponseBody.getStatusCode()).isEqualTo(201);
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executeGetUserByIdAndGet200(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user")
                .uponReceiving("a GET /api/v1/users/{userId} request from an RD - "
                        + "USER PROFILE API")
                .path(IDAM_USER_BY_ID_URL)
                .method(HttpMethod.GET.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserByIdAndGet200")
    public void should_get_user_from_elastic_search_using_id_and_get_200_Response(MockServer mockServer)
            throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .get(mockServer.getUrl() + IDAM_USER_BY_ID_URL)
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

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executePutUserRolesAndGet200(PactDslWithProvider builder) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user with updated roles")
                .uponReceiving("a PUT /api/v1/users/{userId}/roles request from an RD - "
                        + "USER PROFILE API")
                .path(IDAM_USER_ROLES_BY_ID_URL)
                .method(HttpMethod.PUT.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePutUserRolesAndGet200")
    public void should_put_user_roles_and_get_200_Response(MockServer mockServer) throws JSONException {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .put(mockServer.getUrl() + IDAM_USER_ROLES_BY_ID_URL)
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
        assertThat(response.getString("statusMessage")).isEqualTo("11 OK");

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executePostUserRolesAndGet200(PactDslWithProvider builder) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user with updated roles")
                .uponReceiving("a POST /api/v1/users/{userId}/roles request from an RD "
                        + "- USER PROFILE API")
                .path(IDAM_USER_ROLES_BY_ID_URL)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePostUserRolesAndGet200")
    public void should_post_user_roles_and_get_200_Response(MockServer mockServer) throws JSONException {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .post(mockServer.getUrl() + IDAM_USER_ROLES_BY_ID_URL)
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
        assertThat(response.getString("statusMessage")).isEqualTo("11 OK");

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executeDeleteUserRoleAndGet200(PactDslWithProvider builder) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user with updated roles")
                .uponReceiving("a DELETE /api/v1/users/{userId}/roles/{role} request from an RD - "
                        + "USER PROFILE API")
                .path(IDAM_USER_ROLES_BY_ID_URL + "/pui-case-manager")
                .method(HttpMethod.DELETE.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserResponseRoleDeleted())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeDeleteUserRoleAndGet200")
    public void should_delete_user_roles_and_get_200_Response(MockServer mockServer) throws JSONException {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .delete(mockServer.getUrl() + IDAM_USER_ROLES_BY_ID_URL + "/pui-case-manager")
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
        assertThat(response.getString("statusMessage")).isEqualTo("11 OK");

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isEqualTo("pui-organisation-manager");
    }

    @Test
    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executePatchUserDetailsAndGet200(PactDslWithProvider builder) {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        return builder
                .given("Idam successfully returns user with updated details")
                .uponReceiving("a PATCH /api/v1/users/{userId} request from an RD - USER PROFILE API")
                .path(IDAM_USER_BY_ID_URL)
                .method(HttpMethod.PATCH.toString())
                .headers(headers)
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .body(createUserResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePatchUserDetailsAndGet200")
    public void should_patch_user_details_and_get_200_Response(MockServer mockServer) throws JSONException {
        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);

        String actualResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .patch(mockServer.getUrl() + IDAM_USER_BY_ID_URL)
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
        assertThat(response.getString("statusMessage")).isEqualTo("11 OK");

        JSONArray rolesArr = new JSONArray(response.getString("roles"));

        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isEqualTo("pui-organisation-manager");
    }

    private PactDslJsonBody createUserResponse() {
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager")
                .string("pui-case-manager");

        return new PactDslJsonBody()
                .stringType("id", ID)
                .stringType("email", EMAIL)
                .stringType("forename", "Seymore")
                .stringType("surname", "Skinner")
                .object("roles", array)
                .booleanType("active", true)
                .booleanType("pending", false)
                .stringType("repsonseStatusCode", HttpStatus.OK.toString())
                .stringType("statusMessage", "11 OK");

    }

    private PactDslJsonBody createUserResponseRoleDeleted() {
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager");

        return new PactDslJsonBody()
                .stringType("id", ID)
                .stringType("email", EMAIL)
                .stringType("forename", "Seymore")
                .stringType("surname", "Skinner")
                .object("roles", array)
                .booleanType("active", true)
                .booleanType("pending", false)
                .stringType("repsonseStatusCode", HttpStatus.OK.toString())
                .stringType("statusMessage", "11 OK");

    }
}