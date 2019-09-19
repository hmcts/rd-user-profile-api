package uk.gov.hmcts.reform.userprofileapi;

import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.List;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@Ignore
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
    public void should_create_user_profile_for_duplicate_idam_user_and_verify_successfully_for_prd_roles() throws Exception {

        List<String> roles = new ArrayList();
        roles.add("pui-user-manager");
        roles.add("pui-case-manager");

        //create user with "pui-user-manager" role in SIDAM
        String email = idamClient.createUser("pui-user-manager");

        //create User profile with same email to get 409 scenario
        CreateUserProfileData data = createUserProfileData();
        data.setRoles(roles);
        data.setEmail(email);
        CreateUserProfileResponse duplicateUserResource = createUserProfile(data, HttpStatus.CREATED);
        verifyCreateUserProfile(duplicateUserResource);

        //get user by getUserById to check new roles got added in SIDAM
        //should have 2 roles
        String userId = duplicateUserResource.getIdamId();
        GetUserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + userId + "/roles",
                        GetUserProfileWithRolesResponse.class);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");

    }

    @Test
    public void should_create_user_profile_for_duplicate_idam_user_and_verify_roles_updated_successfully_for_citizen() throws Exception {

        //create user with citizen role in SIDAM
        String email = idamClient.createUser("citizen");

        //create user profile in UP with PRD-ADMIN token for above user with same email with pui-user-manager roles
        CreateUserProfileData data = createUserProfileData();
        data.setEmail(email);
        CreateUserProfileResponse duplicateUserResource = createUserProfile(data, HttpStatus.CREATED);
        verifyCreateUserProfile(duplicateUserResource);

        //get user by getUserById to check new roles got added in SIDAM
        String userId = duplicateUserResource.getIdamId();
        GetUserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + userId + "/roles",
                        GetUserProfileWithRolesResponse.class);

        assertThat(resource.getRoles()).contains("citizen");
        assertThat(resource.getRoles()).contains("pui-user-manager");
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
