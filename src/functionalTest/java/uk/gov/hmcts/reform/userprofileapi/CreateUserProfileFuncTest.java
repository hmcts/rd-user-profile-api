package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;

import io.restassured.RestAssured;
import java.util.stream.Stream;
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
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
@ActiveProfiles("functional")
public class CreateUserProfileFuncTest {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Value("${targetInstance}") private String targetInstance;

    @Autowired private Environment environment;
    @Autowired private FuncTestRequestHandler testRequestHandler;

    private String requestUri = "/profiles";

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_verify_create_and_get_using_all_fields_successfully() throws Exception {

        final String email = randomAlphabetic(10) + "@somewhere.com";
        CreateUserProfileData data = new CreateUserProfileData(email,
            randomAlphabetic(20),
            randomAlphabetic(20));

        UserProfileResource createdResource = testRequestHandler.doPostAndVerify(data, HttpStatus.CREATED, requestUri);
        testRequestHandler.doGetAndVerify(createdResource, requestUri + "/" + createdResource.getId());

        String idamIdUrl = requestUri + "?idamId=" + createdResource.getIdamId();
        String emailUrl = requestUri + "?email=" + createdResource.getEmail();

        Stream.of(idamIdUrl, emailUrl)
            .forEach(url -> testRequestHandler.doGetAndVerify(createdResource, url));
    }

    @Test
    public void should_return_400_when_required_email_field_is_missing() {
        String json = "{\"firstName\":\"iWvKhGLXCiOMMbZtngbR\",\"lastName\":\"mXlpNLcbodhABAWKCKbj\"}";
        testRequestHandler.doPost(json, HttpStatus.BAD_REQUEST, requestUri);
    }

    @Test
    public void should_return_404_when_retrieving_user_profile_when_email_param_is_empty() {
        String emailUrl = requestUri + "?email=";
        testRequestHandler.doGet(HttpStatus.NOT_FOUND, emailUrl);
    }

    @Test
    public void should_return_404_when_retrieving_user_profile_when_idamId_param_is_empty() {
        String idamIdUrl = requestUri + "?idamId=";
        testRequestHandler.doGet(HttpStatus.NOT_FOUND, idamIdUrl);

    }

    @Test
    public void should_return_201_when_sending_extra_fields() throws Exception {

        final String email = randomAlphabetic(10) + "@somewhere.com";
        CreateUserProfileData data = new CreateUserProfileData(email,
            randomAlphabetic(20),
            randomAlphabetic(20));

        JSONObject json = new JSONObject(testRequestHandler.getObjectMapper().writeValueAsString(data));
        json.put("extra-field1", randomAlphabetic(20));
        json.put("extra-field2", randomAlphabetic(20));

        LOG.info("json output {} ", json.toString());


        testRequestHandler.doPost(json.toString(), HttpStatus.CREATED, requestUri);
    }

    @Test
    public void should_return_400_and_not_allow_get_request_on_base_url_with_no_params() {
        testRequestHandler.doGet(HttpStatus.BAD_REQUEST, requestUri);
    }

    @Test
    public void should_return_405_when_post_sent_to_wrong_url() throws Exception {
        final String email = randomAlphabetic(10) + "@somewhere.com";
        CreateUserProfileData data = new CreateUserProfileData(email,
            randomAlphabetic(20),
            randomAlphabetic(20));
        JSONObject json = new JSONObject(testRequestHandler.getObjectMapper().writeValueAsString(data));

        testRequestHandler.doPost(json.toString(), HttpStatus.METHOD_NOT_ALLOWED, requestUri + "/id");
    }

    @Test
    public void should_return_400_when_attempting_to_add_duplicate_emails() throws Exception {

        final String email = randomAlphabetic(10) + "@somewhere.com";
        CreateUserProfileData data = new CreateUserProfileData(email,
            randomAlphabetic(20),
            randomAlphabetic(20));

        UserProfileResource createdResource = testRequestHandler.doPostAndVerify(data, HttpStatus.CREATED, requestUri);
        testRequestHandler.doGetAndVerify(createdResource, requestUri + "/" + createdResource.getId());

        testRequestHandler.doPost(testRequestHandler.getObjectMapper().writeValueAsString(data), HttpStatus.BAD_REQUEST, requestUri);

    }

}
