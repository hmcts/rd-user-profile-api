package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.IdamClient;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@RunWith(SpringIntegrationSerenityRunner.class)
public class CreateUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Autowired
    protected TestConfigProperties configProperties;

    private IdamClient idamClient;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        idamClient = new IdamClient(configProperties);
    }

    @Test
    public void should_create_user_profile_and_verify_successfully() throws Exception {

        CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData(), HttpStatus.CREATED);

        verifyCreateUserProfile(createdResource);


    }

    @Test
    public void should_create_user_profile_for_duplicate_idam_user_and_verify_successfully() throws Exception {

        String email = idamClient.createUser("pui-user-manager");

        CreateUserProfileData data = createUserProfileData();
        data.setEmail(email);
        CreateUserProfileResponse duplicateUserResource = createUserProfile(data, HttpStatus.CREATED);
        verifyCreateUserProfile(duplicateUserResource);

    }

    @Test
    public void should_return_201_when_sending_extra_fields() throws Exception {

        JSONObject json = new JSONObject(testRequestHandler.asJsonString(createUserProfileData()));
        json.put("extra-field1", randomAlphabetic(20));
        json.put("extra-field2", randomAlphabetic(20));

        LOG.info("json output {} ", json.toString());

        testRequestHandler.sendPost(json.toString(), HttpStatus.CREATED, requestUri);
    }

    @Test
    public void should_return_409_when_attempting_to_add_duplicate_emails() throws Exception {

        CreateUserProfileData data = createUserProfileData();

        CreateUserProfileResponse createdResource =
            testRequestHandler.sendPost(
                data,
                HttpStatus.CREATED,
                requestUri,
                    CreateUserProfileResponse.class
            );

        assertThat(createdResource).isNotNull();

        testRequestHandler.sendPost(
            testRequestHandler.asJsonString(data),
            HttpStatus.CONFLICT,
            requestUri);
    }

}
