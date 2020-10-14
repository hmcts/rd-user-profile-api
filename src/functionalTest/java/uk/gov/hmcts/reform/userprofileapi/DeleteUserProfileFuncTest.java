package uk.gov.hmcts.reform.userprofileapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.List;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;


@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
public class DeleteUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteUserProfileFuncTest.class);

    private ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_delete_pending_user_profile_successfully_return_204() throws Exception {
        UserProfileCreationData data = createUserProfileData();
        //creating user profile
        UserProfileCreationResponse userProfileResponse = createUserProfile(data, HttpStatus.CREATED);
        List<String> userIds = new ArrayList<String>();
        userIds.add(userProfileResponse.getIdamId());
        //delete user profile
        verifyDeleteUserProfile(userIds, HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);

    }

    @Test
    public void should_delete_multiple_user_profile_successfully_return_204() throws Exception {
        UserProfileCreationData data1 = createUserProfileData();
        //creating user profile
        UserProfileCreationResponse userProfileResponse = createUserProfile(data1, HttpStatus.CREATED);

        UserProfileCreationData data2 = createUserProfileData();
        //creating user profile
        UserProfileCreationResponse userProfileResponseTwo = createUserProfile(data2, HttpStatus.CREATED);

        //user profile two created
        List<String> userIds = new ArrayList<String>();
        userIds.add(userProfileResponse.getIdamId());
        userIds.add(userProfileResponseTwo.getIdamId());
        //delete user profile
        verifyDeleteUserProfile(userIds, HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);

    }

    @Test
    public void should_delete_active_user_profile_successfully_return_204() throws Exception {

        UserProfileCreationData data = createUserProfileData();
        //creating user profile
        UserProfileCreationResponse activeUserProfile = createActiveUserProfile(data);
        verifyCreateUserProfile(activeUserProfile);
        List<String> userIds = new ArrayList<String>();
        userIds.add(activeUserProfile.getIdamId());
        //delete user profile
        verifyDeleteUserProfile(userIds, HttpStatus.NO_CONTENT, HttpStatus.NOT_FOUND);

    }

    @Test
    public void should_not_delete_user_profile_with_unknown_user_Id_return_404() throws Exception {

        UserProfileCreationData data = createUserProfileData();
        //creating user profile
        UserProfileCreationResponse activeUserProfile = createActiveUserProfile(data);
        List<String> userIds = new ArrayList<String>();
        userIds.add(activeUserProfile.getIdamId());
        userIds.add("1234567");
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        //delete user profile
        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                HttpStatus.NOT_FOUND, requestUri);

        //verify user profile deleted or not
        testRequestHandler.sendGet(HttpStatus.OK,
                requestUri + "?userId=" + activeUserProfile.getIdamId());

    }

    private void verifyDeleteUserProfile(List<String> userIds, HttpStatus deleteStatus, HttpStatus getStatus)
            throws Exception {

        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        //delete user profile
        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                deleteStatus, requestUri);

        //verify user profile deleted or not
        testRequestHandler.sendGet(getStatus,
                requestUri + "?userId=" + userIds.get(0));
    }
}