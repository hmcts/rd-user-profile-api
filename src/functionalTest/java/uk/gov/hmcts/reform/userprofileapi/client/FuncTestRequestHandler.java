package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
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

    public <T> T sendPost(Object data, HttpStatus expectedStatus, String path, Class<T> clazz) throws JsonProcessingException {
        return
            sendPost(objectMapper.writeValueAsString(data),
                expectedStatus,
                path)
                .as(clazz);
    }

    public Response sendPost(String jsonBody, HttpStatus expectedStatus, String path) {
        return SerenityRest
            .given()
            .log().all(true)
            .headers(authorizationHeadersProvider.getServiceAuthorization())
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body(jsonBody)
            .when()
            .post(path)
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
            .headers(authorizationHeadersProvider.getServiceAuthorization())
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .when()
            .get(urlPath)
            .then()
            .statusCode(httpStatus.value()).extract().response();
    }

    public String asJsonString(Object source) throws JsonProcessingException {
        return objectMapper.writeValueAsString(source);
    }


}
