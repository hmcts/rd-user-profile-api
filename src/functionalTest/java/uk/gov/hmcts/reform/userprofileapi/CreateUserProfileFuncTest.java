package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import io.restassured.RestAssured;
import java.util.UUID;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.GetUserProfileResponse;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CreateUserProfileFuncTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private FuncTestRequestHandler testRequestHandler;

    private String requestUri = "/v1/userprofile";

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_create_and_get_user_profile_using_all_fields_and_verify_successfully() throws Exception {

        CreateUserProfileData expectedData = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
            testRequestHandler.sendPost(
                expectedData,
                HttpStatus.CREATED,
                requestUri,
                CreateUserProfileResponse.class
            );

        verifyCreateUserProfile(createdResource, expectedData);

        GetUserProfileResponse resource =
            testRequestHandler.sendGet(
                requestUri + "?userId=" + createdResource.getIdamId(),
                    GetUserProfileResponse.class);

        verifyGetUserProfile(resource, expectedData);

        resource =
            testRequestHandler.sendGet(
                    requestUri + "?email=" + expectedData.getEmail(),
                    GetUserProfileResponse.class
            );

        verifyGetUserProfile(resource, expectedData);
    }

    @Test
    public void should_return_400_when_required_email_field_is_missing() {
        String json = "{\"firstName\":\"iWvKhGLXCiOMMbZtngbR\",\"lastName\":\"mXlpNLcbodhABAWKCKbj\"}";
        testRequestHandler.sendPost(json, HttpStatus.BAD_REQUEST, requestUri);
    }

    @Test
    public void should_return_404_when_retrieving_user_profile_when_email_param_is_empty() {
        String emailUrl = requestUri + "?email=";
        testRequestHandler.sendGet(HttpStatus.NOT_FOUND, emailUrl);
    }

    @Test
    public void should_return_201_when_sending_extra_fields() throws Exception {

        CreateUserProfileData data = buildCreateUserProfileData();

        JSONObject json = new JSONObject(testRequestHandler.asJsonString(data));
        json.put("extra-field1", randomAlphabetic(20));
        json.put("extra-field2", randomAlphabetic(20));

        LOG.info("json output {} ", json.toString());


        testRequestHandler.sendPost(json.toString(), HttpStatus.CREATED, requestUri);
    }

    @Test
    public void should_return_400_and_not_allow_get_request_on_base_url_with_no_params() {
        testRequestHandler.sendGet(HttpStatus.BAD_REQUEST, requestUri);
    }

    @Test
    public void should_return_405_when_post_sent_to_wrong_url() throws Exception {
        CreateUserProfileData data = buildCreateUserProfileData();

        JSONObject json = new JSONObject(testRequestHandler.asJsonString(data));

        testRequestHandler.sendPost(
            json.toString(),
            HttpStatus.METHOD_NOT_ALLOWED,
            requestUri + "/id"
        );
    }

    @Test
    public void should_return_400_when_attempting_to_add_duplicate_emails() throws Exception {

        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
            testRequestHandler.sendPost(
                data,
                HttpStatus.CREATED,
                requestUri,
                    CreateUserProfileResponse.class
            );

        assertThat(createdResource).isNotNull();

        GetUserProfileResponse resource =
            testRequestHandler.sendGet(
                    requestUri + "?userId=" + createdResource.getIdamId(),
                    GetUserProfileResponse.class
            );

        assertThat(resource).isNotNull();

        testRequestHandler.sendPost(
            testRequestHandler.asJsonString(data),
            HttpStatus.BAD_REQUEST,
            requestUri);
    }

    private void verifyCreateUserProfile(CreateUserProfileResponse resource, CreateUserProfileData expectedData) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isInstanceOf(UUID.class);
        assertThat(resource.getIdamRegistrationResponse()).isEqualTo(HttpStatus.CREATED.value());
    }

    private void verifyGetUserProfile(GetUserProfileResponse resource, CreateUserProfileData expectedResource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getFirstName()).isEqualTo(expectedResource.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedResource.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedResource.getEmail());

    }

}
