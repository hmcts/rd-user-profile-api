package uk.gov.hmcts.reform.userprofileapi.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Slf4j
public class FuncTestRequestHandler {

    private ObjectMapper objectMapper = new ObjectMapper();

    protected String baseUrl;
    protected String s2sToken;
    public static final String BEARER = "Bearer ";
    public static IdamOpenIdClient idamOpenIdClient;

    public FuncTestRequestHandler(String baseUrl, String s2sToken, IdamOpenIdClient idamOpenIdClient) {
        this.baseUrl = baseUrl;
        this.s2sToken = s2sToken;
        this.idamOpenIdClient = idamOpenIdClient;
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

    public <T> T sendPut(Object data, HttpStatus expectedStatus, String path, Class<T> clazz)
            throws JsonProcessingException {

        return sendPut(objectMapper.writeValueAsString(data),
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
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }

    public <T> T sendDelete(Object data, HttpStatus expectedStatus, String path, Class<T> clazz)
            throws JsonProcessingException {

        return sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path)
                .as(clazz);
    }

    public Response sendDelete(String jsonBody, HttpStatus expectedStatus, String path) {

        return withAuthenticatedRequest()
                .body(jsonBody)
                .delete(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }

    public Response sendDeleteWithoutBody(HttpStatus expectedStatus, String path) {

        return withAuthenticatedRequest()
                .delete(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }

    public <T> T sendGet(String urlPath, Class<T> clazz) {
        return sendGet(HttpStatus.OK, urlPath).as(clazz);
    }

    public Response sendGet(HttpStatus httpStatus, String urlPath) {
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
                .log().all(true)
                .statusCode(httpStatus.value()).extract().response();
    }

    public <T> T getEmailFromHeader(String urlPath, Class<T> clazz, String email) {
        return getEmailFromHeader(HttpStatus.OK, urlPath, email).as(clazz);
    }


    public Response getEmailFromHeader(HttpStatus httpStatus, String urlPath, String email) {
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
                .log().all(true)
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
