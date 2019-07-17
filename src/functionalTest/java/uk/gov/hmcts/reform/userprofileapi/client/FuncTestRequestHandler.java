package uk.gov.hmcts.reform.userprofileapi.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;

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
import uk.gov.hmcts.reform.userprofileapi.util.AuthorizationHeadersProvider;

@Slf4j
@Service
public class FuncTestRequestHandler {

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${targetInstance}")
    protected String baseUrl;

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

        return withUnauthenticatedRequest()
                .body(jsonBody)
                .post(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }

    public void sendPut(Object data, HttpStatus expectedStatus, String path) throws JsonProcessingException {
        sendPut(objectMapper.writeValueAsString(data),
                expectedStatus,
                path);
    }

    public Response sendPut(String jsonBody, HttpStatus expectedStatus, String path) {

        return withUnauthenticatedRequest()
                .body(jsonBody)
                .put(path)
                .then()
                .log().all(true)
                .statusCode(expectedStatus.value()).extract().response();
    }

    public <T> T sendGet(String urlPath, Class<T> clazz) {
        return sendGet(HttpStatus.OK, urlPath).as(clazz);
    }

    public Response sendGet(HttpStatus httpStatus, String urlPath) {

        return SerenityRest
            .given()
            //.headers(authorizationHeadersProvider.getServiceAuthorization())
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .baseUri(baseUrl)
            .when()
            .get(urlPath)
            .then()
            .log().all(true)
            .statusCode(httpStatus.value()).extract().response();
    }

    public String asJsonString(Object source) throws JsonProcessingException {
        return objectMapper.writeValueAsString(source);
    }

    private RequestSpecification withUnauthenticatedRequest() {
        return SerenityRest.given()
                .relaxedHTTPSValidation()
                .baseUri(baseUrl)
                .header("Content-Type", APPLICATION_JSON_UTF8_VALUE)
                .header("Accepts", APPLICATION_JSON_UTF8_VALUE);
    }


}
