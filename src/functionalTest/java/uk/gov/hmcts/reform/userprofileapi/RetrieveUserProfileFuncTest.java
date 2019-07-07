package uk.gov.hmcts.reform.userprofileapi;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfileWithRolesResponse;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public class RetrieveUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveUserProfileFuncTest.class);

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_get_user_profile_by_userId() throws Exception {

        CreateUserProfileData createUserProfileData = createUserProfileData();
        CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData, HttpStatus.CREATED);
        GetUserProfileResponse resource =
            testRequestHandler.sendGet(
                requestUri + "?userId=" + createdResource.getIdamId(),
                    GetUserProfileResponse.class);

        verifyGetUserProfile(resource, createUserProfileData);
    }

    @Test
    public void should_get_user_profile_by_email() throws Exception {

        CreateUserProfileData createUserProfileData = createUserProfileData();
        CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData, HttpStatus.CREATED);
        GetUserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + createUserProfileData.getEmail().toLowerCase(),
                        GetUserProfileResponse.class
                );

        verifyGetUserProfile(resource, createUserProfileData);
    }

    @Test
    public void should_get_user_profile_with_roles_by_userId() throws Exception {

        CreateUserProfileData createUserProfileData = createUserProfileData();
        CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData, HttpStatus.CREATED);
        GetUserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + createdResource.getIdamId() + "/roles",
                        GetUserProfileWithRolesResponse.class);

        verifyGetUserProfileWithRoles(resource, createUserProfileData);
    }

    @Test
    public void should_get_user_profile_with_roles_by_email() throws Exception {

        CreateUserProfileData createUserProfileData = createUserProfileData();
        CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData, HttpStatus.CREATED);
        GetUserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/roles?email=" + createUserProfileData.getEmail().toLowerCase(),
                        GetUserProfileWithRolesResponse.class
                );

        verifyGetUserProfileWithRoles(resource, createUserProfileData);
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
    public void should_return_400_and_not_allow_get_request_on_base_url_with_no_params() {
        testRequestHandler.sendGet(HttpStatus.BAD_REQUEST, requestUri);
    }

}
