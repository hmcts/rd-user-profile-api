package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileResponse;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public class CreateUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_create_user_profile_and_verify_successfully() throws Exception {

        CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData(), HttpStatus.CREATED);

        verifyCreateUserProfile(createdResource);

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
    public void should_return_400_when_attempting_to_add_duplicate_emails() throws Exception {

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
            HttpStatus.BAD_REQUEST,
            requestUri);
    }

}
