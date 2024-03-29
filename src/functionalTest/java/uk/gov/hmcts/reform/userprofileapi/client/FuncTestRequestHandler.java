package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class FuncTestRequestHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    protected String baseUrl;
    protected String s2sToken;
    public static final String BEARER = "Bearer ";
    public static final String COMMON_EMAIL_PATTERN = "@prdfunctestuser.com";
    public static IdamOpenIdClient idamOpenIdClient;

    public FuncTestRequestHandler(String baseUrl, String s2sToken, IdamOpenIdClient idamOpenIdClient) {
        this.baseUrl = baseUrl;
        this.s2sToken = s2sToken;
        FuncTestRequestHandler.idamOpenIdClient = idamOpenIdClient;
    }

    public <T> T sendPost(Object data, HttpStatus expectedStatus, String path, Class<T> clazz)
            throws JsonProcessingException {
        return
                sendPost(objectMapper.writeValueAsString(data),
                        expectedStatus,
                        path)
                        .as(clazz);
    }

    public Response sendPost(String jsonBody, HttpStatus expectedStatus, String path) {
        log.info("User object to be created : {}", jsonBody);
        Response response = withAuthenticatedRequest()
                .body(jsonBody)
                .post(path)
                .andReturn();

        response.then()
                .assertThat()
                .statusCode(expectedStatus.value());

        return response;
    }

    public <T> void sendPut(Object data, HttpStatus expectedStatus, String path, Class<T> clazz)
            throws JsonProcessingException {

        sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path)
                .as(clazz);
    }

    public void sendPut(Object data, HttpStatus expectedStatus, String path) throws JsonProcessingException {
        sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path);
    }

    public Response sendPut(String jsonBody, HttpStatus expectedStatus, String path) {

        return withAuthenticatedRequest()
                .body(jsonBody)
                .put(path)
                .then()
                .assertThat()
                .statusCode(expectedStatus.value()).extract().response();
    }

    public <T> void sendDelete(Object data, HttpStatus expectedStatus, String path, Class<T> clazz)
            throws JsonProcessingException {

        sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path)
                .as(clazz);
    }

    public void sendDelete(String jsonBody, HttpStatus expectedStatus, String path) {

        withAuthenticatedRequest()
                .body(jsonBody)
                .delete(path)
                .then()
                .statusCode(expectedStatus.value()).extract().response();
    }

    public Response sendDeleteWithoutBody(String path) {

        return withAuthenticatedRequest()
                .delete(path).andReturn();
    }

    public <T> T sendGet(String urlPath, Class<T> clazz) {
        return getUserProfileResponse(HttpStatus.OK, urlPath).as(clazz);
    }

    public Response getUserProfileResponse(HttpStatus httpStatus, String urlPath) {
        String bearerToken = getBearerToken();

        log.info("S2S Token : {}, Bearer Token : {}", s2sToken, bearerToken);

        return SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .baseUri(baseUrl)
                .header("ServiceAuthorization", BEARER + s2sToken)
                .header("Authorization", BEARER + bearerToken)
                .when()
                .get(urlPath)
                .then()
                .statusCode(httpStatus.value()).extract().response();
    }

    public <T> T getUserByEmailInHeaderWithRoles(String urlPath, String email, HttpStatus httpStatus, Class<T> clazz) {
        String bearerToken = getBearerToken();

        log.info("S2S Token : {}, Bearer Token : {}", s2sToken, bearerToken);

        return SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .baseUri(baseUrl)
                .header("ServiceAuthorization", BEARER + s2sToken)
                .header("Authorization", BEARER + bearerToken)
                .header("UserEmail", email)
                .when()
                .get(urlPath)
                .then()
                .statusCode(httpStatus.value()).extract().response()
                .as(clazz);
    }

    public <T> T getUserProfileByEmailFromHeader(String urlPath, Class<T> clazz, String email) {
        return getUserProfileByEmailFromHeader(HttpStatus.OK, urlPath, email).as(clazz);
    }

    public Response getUserProfileByEmailFromHeader(HttpStatus httpStatus, String urlPath, String email) {
        String bearerToken = getBearerToken();

        log.info("S2S Token : {}, Bearer Token : {}", s2sToken, bearerToken);

        return SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .baseUri(baseUrl)
                .header("ServiceAuthorization", BEARER + s2sToken)
                .header("Authorization", BEARER + bearerToken)
                .header("UserEmail", email)
                .when()
                .get(urlPath)
                .then()
                .statusCode(httpStatus.value()).extract().response();
    }

    public String asJsonString(Object source) throws JsonProcessingException {
        return objectMapper.writeValueAsString(source);
    }

    private RequestSpecification withAuthenticatedRequest() {
        String bearerToken = getBearerToken();

        log.info("Base Url : {}", baseUrl);

        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(baseUrl)
                .header("ServiceAuthorization", BEARER + s2sToken)
                .header("Authorization", BEARER + bearerToken)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE);
    }

    private String getBearerToken() {
        return idamOpenIdClient.getBearerToken();
    }
}
