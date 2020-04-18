package uk.gov.hmcts.reform.userprofileapi.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@Slf4j
@Service
public class FuncTestRequestHandler {

    @Autowired
    private TestConfigProperties testConfig;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Value("${targetInstance}")
    protected String baseUrl;

    @Value("${s2s.auth.secret}")
    protected String s2sSecret;

    @Value("${s2s.auth.url}")
    protected String s2sBaseUrl;

    @Value("${s2s.auth.microservice:rd_user_profile_api}")
    protected String s2sMicroservice;

    public static final String BEARER = "Bearer ";


    public <T> T sendPost(Object data, HttpStatus expectedStatus, String path, Class<T> clazz) throws JsonProcessingException {
        return
            sendPost(objectMapper.writeValueAsString(data),
                expectedStatus,
                path)
                .as(clazz);
    }

    public void sendPost(Object data, HttpStatus expectedStatus, String path) throws JsonProcessingException {

        sendPost(objectMapper.writeValueAsString(data),
                expectedStatus,
                path);
    }

    public Response sendPost(String jsonBody, HttpStatus expectedStatus, String path) {
        log.info("User object to be created : {}", jsonBody);
        return withAuthenticatedRequest()
                .body(jsonBody)
                .post(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }


    public <T> T sendPostWithoutToken(Object data, HttpStatus expectedStatus, String path, Class<T> clazz) throws JsonProcessingException {
        return
                sendPostWithoutToken(objectMapper.writeValueAsString(data),
                        expectedStatus,
                        path)
                        .as(clazz);
    }

    public void sendPostWithoutToken(Object data, HttpStatus expectedStatus, String path) throws JsonProcessingException {

        sendPostWithoutToken(objectMapper.writeValueAsString(data),
                expectedStatus,
                path);
    }

    public Response sendPostWithoutToken(String jsonBody, HttpStatus expectedStatus, String path) {

        return withUnauthenticatedBearerRequest()
                .body(jsonBody)
                .post(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }


    public <T> T sendPut(Object data, HttpStatus expectedStatus, String path, Class<T> clazz) throws JsonProcessingException {

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

    public <T> T sendDelete(Object data, HttpStatus expectedStatus, String path, Class<T> clazz) throws JsonProcessingException {

        return sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path)
                .as(clazz);
    }

    public void sendDelete(Object data, HttpStatus expectedStatus, String path) throws JsonProcessingException {
        sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path);
    }

    public Response sendDelete(String jsonBody, HttpStatus expectedStatus, String path) {

        return withAuthenticatedRequest()
                .body(jsonBody)
                .delete(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }

    public <T> T sendGet(String urlPath, Class<T> clazz) {
        return sendGet(HttpStatus.OK, urlPath).as(clazz);
    }

    public Response sendGet(HttpStatus httpStatus, String urlPath) {
        String s2sToken = getS2sToken();
        String bearerToken = getBearerToken();

        log.info("S2S Token : {}, Bearer Token : {}", s2sToken, bearerToken);

        return SerenityRest
            .given()
            //.headers(authorizationHeadersProvider.getServiceAuthorization())
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

    public String asJsonString(Object source) throws JsonProcessingException {
        return objectMapper.writeValueAsString(source);
    }

    private RequestSpecification withAuthenticatedRequest() {
        String s2sToken = getS2sToken();
        String bearerToken = getBearerToken();

        log.info("S2S Token : {}, Bearer Token : {}", s2sToken, bearerToken);

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
        IdamOpenIdClient idamClient = new IdamOpenIdClient(testConfig);
        return idamClient.getBearerToken();
    }

    private String getS2sToken() {
        log.info("S2s Base url : {}, Microservice : {}, Secret : {}", s2sBaseUrl, s2sMicroservice, s2sSecret);
        S2sClient client = new S2sClient(s2sBaseUrl, s2sMicroservice, s2sSecret);
        return client.getS2sToken();
    }


    private RequestSpecification withUnauthenticatedBearerRequest() {
        String s2sToken = getS2sToken();
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(baseUrl)
                .header("ServiceAuthorization", BEARER + s2sToken)
                .header("Authorization", BEARER)
                .header("Content-Type", APPLICATION_JSON_VALUE)
                .header("Accepts", APPLICATION_JSON_VALUE);
    }

}
