package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;
import java.util.stream.Stream;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.serenitybdd.rest.SerenityRest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.serialization.StdSerializer;
import uk.gov.hmcts.reform.userprofileapi.util.AuthorizationHeadersProvider;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CreateUserProfileFunctionalTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFunctionalTest.class);

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private Environment environment;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuthorizationHeadersProvider authorizationHeadersProvider;

    @Autowired private StdSerializer<CreateUserProfileData> serializer;

    private String requestUri = "/profiles";

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_verify_create_and_get_using_all_fields_successfully() {

        final String email = randomAlphabetic(10) + "@somewhere.com";
        CreateUserProfileData data = new CreateUserProfileData(email,
            randomAlphabetic(20),
            randomAlphabetic(20));

        UserProfileResource createdResource = verifyPost(data);
        verifyGet(createdResource, requestUri + "/" + createdResource.getId());

        String idamIdUrl = requestUri + "?idamId=" + createdResource.getIdamId();
        String emailUrl = requestUri + "?email=" + createdResource.getEmail();

        Stream.of(idamIdUrl, emailUrl)
            .forEach(url -> verifyGet(createdResource, url));
    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_required_field_is_missing() {
        String json = "{\"firstName\":\"iWvKhGLXCiOMMbZtngbR\",\"lastName\":\"mXlpNLcbodhABAWKCKbj\"}";
        verifyPost(HttpStatus.BAD_REQUEST, json);
    }

    @Test
    public void should_return_400_when_retrieving_user_profile_when_email_param_not_correctly_provided() {
        String emailUrl = requestUri + "?email=";
        verifyGet(HttpStatus.NOT_FOUND, emailUrl);
    }

    @Test
    public void should_return_400_when_retrieving_user_profile_when_idamId_param_not_correctly_provided() {
        String idamIdUrl = requestUri + "?idamId=";
        verifyGet(HttpStatus.NOT_FOUND, idamIdUrl);

    }

    private UserProfileResource verifyPost(CreateUserProfileData data) {
        final String json = serializer.serialize(data);
        LOG.info("json request: {}", json);

        UserProfileResource createdResource =
            SerenityRest
                .given()
                .headers(authorizationHeadersProvider.getServiceAuthorization())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .body(json)
                .when()
                .post(requestUri)
                .then()
                .statusCode(HttpStatus.CREATED.value())
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

    private void verifyPost(HttpStatus expectedStatus, String json) {

        SerenityRest
            .given()
            .headers(authorizationHeadersProvider.getServiceAuthorization())
            .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
            .body(json)
            .when()
            .post(requestUri)
            .then()
            .statusCode(expectedStatus.value());

    }

    private UserProfileResource verifyGet(UserProfileResource expectation, String urlPath) {

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

    private void verifyGet(HttpStatus httpStatus, String urlPath) {

            SerenityRest
                .given()
                .headers(authorizationHeadersProvider.getServiceAuthorization())
                .contentType(MediaType.APPLICATION_JSON_UTF8_VALUE)
                .when()
                .get(urlPath)
                .then()
                .statusCode(httpStatus.value());
    }


}
