package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.google.gson.Gson;

import com.google.gson.annotations.SerializedName;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@Slf4j
public class IdamClient {

    private final TestConfigProperties testConfig;

    public static final String BASIC = "Basic ";

    private final String password = "Hmcts123";

    private Gson gson = new Gson();

    public IdamClient(TestConfigProperties testConfig) {
        this.testConfig = testConfig;
    }

    public String createUser(List<String> roles) {
        //Generating a random user
        String userEmail = nextUserEmail();
        String firstName = "First";
        String lastName = "Last";
        String password = "Hmcts123";

        String id = UUID.randomUUID().toString();

        List<Role> rolesList = roles.stream().map(role -> new Role(role)).collect(Collectors.toList());

        User user = new User(userEmail, firstName, id, lastName, password, rolesList);

        String serializedUser = gson.toJson(user);

        Response createdUserResponse = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(serializedUser)
                .post("/testing-support/accounts")
                .andReturn();


        assertThat(createdUserResponse.getStatusCode()).isEqualTo(201);

        return userEmail;
    }

    public String getBearerToken() {

        List<String> roles = new ArrayList<>();
        roles.add("prd-admin");
        String userEmail = createUser(roles);

        String codeAuthorization = Base64.getEncoder().encodeToString((userEmail + ":" + password).getBytes());

        log.info("User Authorization code::" + codeAuthorization);

        Map<String, String> authorizeParams = new HashMap<>();
        authorizeParams.put("client_id", testConfig.getClientId());
        authorizeParams.put("redirect_uri", testConfig.getOauthRedirectUrl());
        authorizeParams.put("response_type", "code");
        authorizeParams.put("scope", "openid profile roles manage-user create-user search-user");

        log.info("authorizeParams::" + authorizeParams);

        Response authorizeResponse = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", BASIC + codeAuthorization)
                .params(authorizeParams)
                .post("/oauth2/authorize")
                .andReturn();

        assertThat(authorizeResponse.getStatusCode()).isEqualTo(200);

        AuthorizationResponse authorizationCode = gson.fromJson(authorizeResponse.getBody().asString(),
                AuthorizationResponse.class);

        String authCode = authorizationCode.getCode();

        Map<String, String> tokenParams = new HashMap<>();
        tokenParams.put("client_id", testConfig.getClientId());
        tokenParams.put("code", authCode);
        tokenParams.put("grant_type", "authorization_code");
        tokenParams.put("redirect_uri", testConfig.getOauthRedirectUrl());

        Response bearerTokenResponse = RestAssured
                .given()
                .relaxedHTTPSValidation()
                .baseUri(testConfig.getIdamApiUrl())
                .header(CONTENT_TYPE, APPLICATION_FORM_URLENCODED_VALUE)
                .header("Authorization", BASIC + testConfig.getTokenAuthorization())
                .params(tokenParams)
                .post("/oauth2/token")
                .andReturn();

        assertThat(bearerTokenResponse.getStatusCode()).isEqualTo(200);

        BearerTokenResponse accessTokenResponse = gson.fromJson(bearerTokenResponse.getBody().asString(),
                BearerTokenResponse.class);
        return accessTokenResponse.getAccessToken();

    }


    private String nextUserEmail() {
        return String.format(testConfig.getGeneratedUserEmailPattern(),
                RandomStringUtils.randomAlphanumeric(10));
    }

    @AllArgsConstructor
    class User {
        private String email;
        private String forename;
        private String id;
        private String surname;
        private String password;
        private List<Role> roles;
    }

    @AllArgsConstructor
    class Role {
        private String code;
    }

    @AllArgsConstructor
    class Group {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    class AuthorizationResponse {
        private String code;
    }

    @Getter
    @AllArgsConstructor
    class BearerTokenResponse {
        @SerializedName("access_token")
        private String accessToken;
    }
}
