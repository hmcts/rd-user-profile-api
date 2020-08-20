package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.DslPart;
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
import java.util.TreeMap;
import java.util.UUID;

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
    private static final String FIRST_NAME = "firstName";
    private static final String LAST_NAME = "lastName";
    private static final String ROLES = "roles";

    private static final String IDAM_POST_USER_REGISTRATION_URL = "/api/v1/users/registration";
    private static final String IDAM_USER_BY_ID_URL = "/api/v1/users/" + ID;
    private static final String IDAM_USER_ROLES_BY_ID_URL = "/api/v1/users/" + ID + "/roles";
    private static final String ACCESS_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiMWVyMFdSd2dJT1RBRm9qR"
            + "TRyQy9mYmVLdTNJPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzb3VyYXYuYmhhdHRhY2hhcnlhQGhtY3RzLm5ldCIsImN0cyI6Ik9BV"
            + "VRIMl9TVEFURUxFU1NfR1JBTlQiLCJhdXRoX2xldmVsIjowLCJhdWRpdFRyYWNraW5nSWQiOiJhYjhlM2VlNi02ZjE1LTQ1MjItOTQzNC0yYzY0ZGJmZDcwYzAtMTI5MDg2NzIiLCJpc3MiOiJodHRwczovL2Zvcmdlcm9jay1hbS5zZXJ2aWNlLmNvcmUtY29tcHV0ZS1pZGFtLWFhdDIuaW50ZXJuYWw6ODQ0My9vcGVuYW0vb2F1dGgyL2htY3RzIiwidG9rZW5OYW1lIjoiYWNjZXNzX3Rva2VuIiwidG9rZW5fdHlwZSI6IkJlYXJlciIsImF1dGhHcmFudElkIjoidXBXUk5tVkI3dEd2blJhNkUtZ1RnaFNGUDNzIiwiYXVkIjoicmQtcHJvZmVzc2lvbmFsLWFwaSIsIm5iZiI6MTU5MzAxODI3MywiZ3JhbnRfdHlwZSI6InBhc3N3b3JkIiwic2NvcGUiOlsib3BlbmlkIiwicHJvZmlsZSIsInJvbGVzIiwiY3JlYXRlLXVzZXIiLCJtYW5hZ2UtdXNlciIsInNlYXJjaC11c2VyIl0sImF1dGhfdGltZSI6MTU5MzAxODI3MywicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1OTMwNDcwNzMsImlhdCI6MTU5MzAxODI3MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IlBrdXZPS1VaQ1pDNmNHb19fcHRBV18tSE1CMCJ9.SOPRnE4GgQuoHA3jfnmhaClGzImQbJKPzlXuoT0TgaPa9RGk0oxFmBlPbniE1WquUPfiD1MJq-8TYbyW8mp0f7rMV11d3JuBezHbWvl1CWTY8CFB3UP-SWQdVcTioxci7jlib8klFo2fwnq3B1F73VybRxZ5h4EZe6ENvSFXKyW_EheJOLDmpmuS-0-DLy8O7rgVfWBiuSx9pn6kXkZgC3yRqakgN6d22oP8iIe1YnarDrmb2XTuPNogNCzlTeTfpQ1aY66VQkcr_cS4FKOMP1EtFh9b9SaZd3FcYbbCZk4IB0AkDkPdFE-1bCa2COIYbAI0bPNssx9fRqT4E2aUQg";


    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api__Idam_api")
    public RequestResponsePact executePostRegistrationAndGet201(PactDslWithProvider builder) {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put("Content-Type", "application/json");

        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");

        Map<String, Object> formParam = new TreeMap<>();
        formParam.put("redirect_uri", "http://www.dummy-pact-service.com/callback");
        formParam.put("client_id", "pact");
        formParam.put("grant_type", "password");
        formParam.put("username", "prdadmin@email.net");
        formParam.put("password", "Password123");
        formParam.put("client_secret", "pactsecret");
        formParam.put("scope", "openid profile roles manage-user create-user search-user");

        return builder
                .given("I have obtained an access_token as a user", formParam)
                .uponReceiving("a POST /registration request from an RD - USER PROFILE API")
                .path(IDAM_POST_USER_REGISTRATION_URL)
                .method(HttpMethod.POST.toString())
                .headers(headers)
                .body("{"
                        + " \"email\": \"pact@test.com\","
                        + " \"firstName\": \"up\","
                        + " \"id\": \"e65e5439-a8f7-4ae6-b378-cc1015b72dbb\","
                        + " \"lastName\": \"rd\","
                        + " \"roles\": ["
                        + "  \"pui-organisation-manager\","
                        + "  \"pui-user-manager\""
                        + "]"
                        + "}")
                .willRespondWith()
                .headers(responseHeaders)
                .status(HttpStatus.CREATED.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePostRegistrationAndGet201")
    public void should_post_for_registration_and_return_201(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put("Content-Type", "application/json");

        Map<String, String> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type", "application/json");

        Response actualResponseBody =
                SerenityRest
                        .given()
                        // .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .when()
                        .headers(headers)
                        .body("{"
                                  + " \"email\": \"pact@test.com\","
                                  + " \"firstName\": \"up\","
                                  + " \"id\": \"e65e5439-a8f7-4ae6-b378-cc1015b72dbb\","
                                  + " \"lastName\": \"rd\","
                                  + " \"roles\": ["
                                  + "  \"pui-organisation-manager\","
                                  + "  \"pui-user-manager\""
                                  + "]"
                                + "}")
                        .post(mockServer.getUrl() + IDAM_POST_USER_REGISTRATION_URL)
                        .then()
                        .statusCode(201)
                        .headers(responseHeaders)
                        .and()
                        .extract()
                        .response();

        assertThat(actualResponseBody.getStatusCode()).isEqualTo(201);
    }


    private PactDslJsonBody createUserProfileRequest() {
        boolean status = true;
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager")
                .string("pui-case-manager");
        return new PactDslJsonBody()
                .stringType("email", "rdup@spookmail.com")
                .stringType(FIRST_NAME, "up")
                .stringType("id", UUID.randomUUID().toString())
                .stringType(LAST_NAME, "rd")
                .stringType(ROLES, array.toString());
    }


    private DslPart createUserResponse() {
        PactDslJsonArray array = new PactDslJsonArray()
                .string("pui-organisation-manager")
                .string("pui-case-manager");

        return PactDslJsonArray.arrayEachLike(1)
                .stringType("id", ID)
                .stringType("email", EMAIL)
                .stringType("forename", "Seymore")
                .stringType("surname", "Skinner")
                .object("roles", array)
                .booleanType("pending", false)
                .stringType("repsonseStatusCode", HttpStatus.OK.toString())
                .stringType("statusMessage", "11 OK")
                .closeObject();
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