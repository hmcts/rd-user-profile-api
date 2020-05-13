package uk.gov.hmcts.reform.userprofileapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.List;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.IdamClient;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;


@RunWith(SpringIntegrationSerenityRunner.class)
public class DeleteUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserProfileFuncTest.class);

    @Autowired
    protected TestConfigProperties configProperties;

    private IdamClient idamClient;
    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        idamClient = new IdamClient(configProperties);
    }

    @Test
    public void should_delete_pending_user_profile_successfully_return_204() throws Exception {
        UserProfileCreationData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);
        data.setEmail(email);
        //creating user profile
        UserProfileCreationResponse userProfileResponse = createUserProfile(data, HttpStatus.CREATED);

        List<String> userIds = new ArrayList<String>();
        userIds.add(userProfileResponse.getIdamId());
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        //delete user profile
        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                HttpStatus.NO_CONTENT, requestUri);

        //verify user profile deleted or not
        testRequestHandler.sendGet(HttpStatus.NOT_FOUND,
                requestUri + "?userId=" + userProfileResponse.getIdamId());
    }

    @Test
    public void should_delete_active_user_profile_successfully_return_204() throws Exception {

        UserProfileCreationData data = createUserProfileData();
        //creating user profile
        UserProfileCreationResponse activeUserProfile = createActiveUserProfile(data);
        verifyCreateUserProfile(activeUserProfile);

        List<String> userIds = new ArrayList<String>();
        userIds.add(activeUserProfile.getIdamId());
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        //delete user profile
        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                HttpStatus.NO_CONTENT, requestUri);

        //verify user profile deleted or not
        testRequestHandler.sendGet(HttpStatus.NOT_FOUND,
                requestUri + "?userId=" + activeUserProfile.getIdamId());
    }

    @Test
    public void should_not_delete_user_profile_with_unknown_user_Id_return_404() throws Exception {

        List<String> userIds = new ArrayList<String>();
        userIds.add("1234567");
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        //delete user profile
        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                HttpStatus.NOT_FOUND, requestUri);
    }
}