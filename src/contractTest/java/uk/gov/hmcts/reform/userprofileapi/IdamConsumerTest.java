package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.dsl.DslPart;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.model.RequestResponsePact;
import com.google.common.collect.Maps;
import groovy.util.logging.Slf4j;
import io.restassured.response.Response;
import java.util.Map;
import java.util.TreeMap;

import net.serenitybdd.rest.SerenityRest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
    private static final String IDAM_USER_BY_EMAIL = "/api/v1/users";
    private static final String IDAM_USERINFO_URL = "/o/userinfo";

    private static final String ACCESS_TOKEN = "Bearer eyJ0eXAiOiJKV1QiLCJraWQiOiJiL082T3ZWdeRre";


    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api")
    public RequestResponsePact executeGetUserInfoDetailsAndGet200(PactDslWithProvider builder) {

        Map<String, String> requestHeaders = Maps.newHashMap();
        requestHeaders.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        Map<String, Object> params = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        params.put("redirect_uri", "http://www.dummy-pact-service.com/callback");
        params.put("client_id", "pact");
        params.put("client_secret", "pactsecret");
        params.put("scope", "openid profile roles");
        params.put("username", "prdadmin@email.net");
        params.put("password", "Password123");

        Map<String, String> responseheaders = Maps.newHashMap();
        responseheaders.put("Content-Type", "application/json");

        return builder
                .given("I have obtained an access_token as a user",params)
                .uponReceiving("Provider returns user info to RD - USER PROFILE API")
                .path(IDAM_USERINFO_URL)
                .headers("Authorization",ACCESS_TOKEN)
                .method(HttpMethod.GET.toString())
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .headers(responseheaders)
                .body(createUserInfoResponse())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executeGetUserInfoDetailsAndGet200")
    public void should_get_user_info_details_with_access_token(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        String detailsResponseBody =
                SerenityRest
                        .given()
                        .headers(headers)
                        .when()
                        .get(mockServer.getUrl() + IDAM_USERINFO_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .body()
                        .asString();

        JSONObject response = new JSONObject(detailsResponseBody);

        assertThat(detailsResponseBody).isNotNull();
        assertThat(response).hasNoNullFieldsOrProperties();
        assertThat(response.getString("uid")).isNotBlank();
        assertThat(response.getString("given_name")).isNotBlank();
        assertThat(response.getString("family_name")).isNotBlank();
        JSONArray rolesArr = response.getJSONArray("roles");
        assertThat(rolesArr).isNotNull();
        assertThat(rolesArr.length()).isNotZero();
        assertThat(rolesArr.get(0).toString()).isNotBlank();

    }

    private DslPart createUserInfoResponse() {

        return new PactDslJsonBody()
                .stringType("uid", "1111-2222-3333-4567")
                .stringType("given_name", "puiCaseManager")
                .stringType("family_name", "Jar")
                .array("roles")
                .stringType("prd-admin")
                .stringType("IDAM_ADMIN_USER")
                .closeArray();

    }


    @Pact(provider = "Idam_api", consumer = "rd_user_profile_api")
    public RequestResponsePact executePostRegistrationAndGet200(PactDslWithProvider builder) {

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
                        + "  \"pui-case-manager\","
                        + "  \"pui-user-manager\""
                        + "]"
                        + "}")
                .willRespondWith()
                .status(HttpStatus.OK.value())
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "executePostRegistrationAndGet200")
    public void should_post_for_registration_and_return_200(MockServer mockServer) throws JSONException {

        Map<String, String> headers = Maps.newHashMap();
        headers.put(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.put("Content-Type", "application/json");

        Response actualResponseBody =
                SerenityRest
                        .given()
                        .when()
                        .headers(headers)
                        .body("{"
                                  + " \"email\": \"pact@test.com\","
                                  + " \"firstName\": \"up\","
                                  + " \"id\": \"e65e5439-a8f7-4ae6-b378-cc1015b72dbb\","
                                  + " \"lastName\": \"rd\","
                                  + " \"roles\": ["
                                  + "  \"pui-case-manager\","
                                  + "  \"pui-user-manager\""
                                  + "]"
                                + "}")
                        .post(mockServer.getUrl() + IDAM_POST_USER_REGISTRATION_URL)
                        .then()
                        .statusCode(200)
                        .and()
                        .extract()
                        .response();

        assertThat(actualResponseBody.getStatusCode()).isEqualTo(200);
    }

}