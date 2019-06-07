package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import io.restassured.RestAssured;
import java.time.LocalDateTime;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.domain.CreationChannel;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CreateUserProfileFuncTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private FuncTestRequestHandler testRequestHandler;

    private String requestUri = "/profiles";

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_create_and_get_user_profile_using_all_fields_and_verify_successfully() throws Exception {

        CreateUserProfileData expectedData = buildCreateUserProfileData();

        UserProfileResource createdResource =
            testRequestHandler.sendPost(
                expectedData,
                CREATED,
                requestUri,
                UserProfileResource.class
            );

        verifyCreateUserProfile(createdResource, expectedData);

        UserProfileResource resource =
            testRequestHandler.sendGet(
                requestUri + "/" + createdResource.getId(),
                UserProfileResource.class);

        verifyGetUserProfile(resource, createdResource);

        String emailUrl = "";//requestUri + "?email=" + createdResource.getEmail();

        resource =
            testRequestHandler.sendGet(
                emailUrl,
                UserProfileResource.class
            );

        verifyGetUserProfile(resource, createdResource);
    }

    @Test
    public void should_return_400_when_required_email_field_is_missing() {
        String json = "{\"firstName\":\"iWvKhGLXCiOMMbZtngbR\",\"lastName\":\"mXlpNLcbodhABAWKCKbj\"}";
        testRequestHandler.sendPost(json, BAD_REQUEST, requestUri);
    }

    @Test
    public void should_return_404_when_retrieving_user_profile_when_email_param_is_empty() {
        String emailUrl = requestUri + "?email=";
        testRequestHandler.sendGet(NOT_FOUND, emailUrl);
    }

    @Test
    public void should_return_201_when_sending_extra_fields() throws Exception {

        CreateUserProfileData data = buildCreateUserProfileData();

        JSONObject json = new JSONObject(testRequestHandler.asJsonString(data));
        json.put("extra-field1", randomAlphabetic(20));
        json.put("extra-field2", randomAlphabetic(20));

        LOG.info("json output {} ", json.toString());


        testRequestHandler.sendPost(json.toString(), CREATED, requestUri);
    }

    @Test
    public void should_return_400_and_not_allow_get_request_on_base_url_with_no_params() {
        testRequestHandler.sendGet(BAD_REQUEST, requestUri);
    }

    @Test
    public void should_return_405_when_post_sent_to_wrong_url() throws Exception {
        CreateUserProfileData data = buildCreateUserProfileData();

        JSONObject json = new JSONObject(testRequestHandler.asJsonString(data));

        testRequestHandler.sendPost(
            json.toString(),
            METHOD_NOT_ALLOWED,
            requestUri + "/id"
        );
    }

    @Test
    public void should_return_400_when_attempting_to_add_duplicate_emails() throws Exception {

        CreateUserProfileData data = buildCreateUserProfileData();

        UserProfileResource createdResource =
            testRequestHandler.sendPost(
                data,
                CREATED,
                requestUri,
                UserProfileResource.class
            );

        assertThat(createdResource).isNotNull();

        UserProfileResource resource =
            testRequestHandler.sendGet(
                requestUri + "/" + createdResource.getId(),
                UserProfileResource.class
            );

        assertThat(resource).isNotNull();

        testRequestHandler.sendPost(
            testRequestHandler.asJsonString(data),
            BAD_REQUEST,
            requestUri);
    }

    private void verifyCreateUserProfile(UserProfileResource resource, CreateUserProfileData expectedData) {

        assertThat(resource).isNotNull();

        assertThat(resource.getId()).isNotNull();
      /*  assertThat(resource.getFirstName()).isEqualTo(expectedData.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedData.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedData.getEmail());
        assertThat(resource.getLanguagePreference()).isEqualTo(resource.getLanguagePreference());

        assertThat(resource.isEmailCommsConsent()).isEqualTo(expectedData.isEmailCommsConsent());
        assertThat(resource.getEmailCommsConsentTs()).isBetween(LocalDateTime.now().minusSeconds(30), LocalDateTime.now());
        assertThat(resource.isPostalCommsConsent()).isEqualTo(expectedData.isPostalCommsConsent());
        assertThat(resource.getPostalCommsConsentTs()).isBetween(LocalDateTime.now().minusSeconds(30), LocalDateTime.now());

        assertThat(resource.getCreationChannel()).isEqualTo(CreationChannel.API.toString());
        assertThat(resource.getUserCategory()).isEqualTo(expectedData.getUserCategory());
        assertThat(resource.getUserType()).isEqualTo(expectedData.getUserType());

        assertThat(resource.getIdamId()).isNull();
        assertThat(resource.getIdamStatus()).isNull();
        assertThat(resource.getIdamRoles()).isEqualTo(expectedData.getIdamRoles());
        assertThat(resource.getIdamRegistrationResponse()).isEqualTo(ACCEPTED.value());*/

    }

    private void verifyGetUserProfile(UserProfileResource resource, UserProfileResource expectedResource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getId()).isEqualTo(expectedResource.getId());
        /*assertThat(resource.getIdamId()).isEqualTo(expectedResource.getIdamId());
        assertThat(resource.getFirstName()).isEqualTo(expectedResource.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedResource.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedResource.getEmail());*/

    }

}
