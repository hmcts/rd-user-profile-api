package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import net.serenitybdd.rest.SerenityRest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.util.AuthorizationHeadersProvider;

@Service
public class FuncTestRequestHandler {

    private static final Logger LOG = LoggerFactory.getLogger(FuncTestRequestHandler.class);

    @Autowired
    private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired
    private ObjectMapper objectMapper;

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public UserProfileResource doPostAndVerify(CreateUserProfileData data, HttpStatus expectedStatus, String path) throws JsonProcessingException {
        final String json = objectMapper.writeValueAsString(data);
        LOG.info("json request: {}", json);

        UserProfileResource createdResource =
            SerenityRest
                .given()
                .headers(authorizationHeadersProvider.getServiceAuthorization())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(json)
                .when()
                .post(path)
                .then()
                .statusCode(expectedStatus.value())
                .and()
                .extract()
                .body()
                .as(UserProfileResource.class);

        assertThat(createdResource).isNotNull();
        assertThat(createdResource.getId()).isNotNull();
        assertThat(createdResource.getIdamId()).isNotEmpty();
        assertThat(createdResource.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(createdResource.getLastName()).isEqualTo(data.getLastName());
        assertThat(createdResource.getEmail()).isEqualTo(data.getEmail());

        return createdResource;

    }

    public Response doPost(String json, HttpStatus expectedStatus, String path) {

        return SerenityRest
            .given()
            .headers(authorizationHeadersProvider.getServiceAuthorization())
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body(json)
            .when()
            .post(path)
            .then()
            .statusCode(expectedStatus.value()).extract().response();
    }

    public UserProfileResource doGetAndVerify(UserProfileResource expectation, String urlPath) {

        UserProfileResource resource =
            SerenityRest
                .given()
                .headers(authorizationHeadersProvider.getServiceAuthorization())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when()
                .get(urlPath)
                .then()
                .statusCode(HttpStatus.OK.value())
                .and()
                .extract()
                .body()
                .as(UserProfileResource.class);

        assertThat(resource).isNotNull();
        assertThat(resource.getId()).isEqualTo(expectation.getId());
        assertThat(resource.getIdamId()).isEqualTo(expectation.getIdamId());
        assertThat(resource.getFirstName()).isEqualTo(expectation.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectation.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectation.getEmail());

        return resource;

    }

    public Response doGet(HttpStatus httpStatus, String urlPath) {

        return SerenityRest
            .given()
            .headers(authorizationHeadersProvider.getServiceAuthorization())
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .when()
            .get(urlPath)
            .then()
            .statusCode(httpStatus.value()).extract().response();
    }

}
