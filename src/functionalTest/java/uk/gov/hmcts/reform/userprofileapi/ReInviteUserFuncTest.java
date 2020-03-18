package uk.gov.hmcts.reform.userprofileapi;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(SpringIntegrationSerenityRunner.class)
public class ReInviteUserFuncTest extends AbstractFunctional {

    //AC3: resend invite to a given user who does not exist
    @Test
    public void should_return_404_when_user_reinvited_if_user_not_exists() throws Exception {

        testRequestHandler.sendPost(
                testRequestHandler.asJsonString(createUserProfileDataWithReInvite()),
                HttpStatus.NOT_FOUND,
                requestUri);

    }

    //AC4: resend invite to a given user who is not in the 'Pending' state
    @Test
    public void should_return_400_when_user_reinvited_if_user_is_active() throws Exception {

        UserProfileCreationData activeUserData = createUserProfileData();
        UserProfileCreationResponse duplicateUserResource = createActiveUserProfile(activeUserData);
        verifyCreateUserProfile(duplicateUserResource);

        UserProfileCreationData data = createUserProfileDataWithReInvite();
        data.setEmail(activeUserData.getEmail());

        testRequestHandler.sendPost(
                testRequestHandler.asJsonString(data),
                HttpStatus.BAD_REQUEST,
                requestUri);
    }

    //AC5: resend invite to a given user who was last invited less than one hour before
    @Test
    public void should_return_429_when_user_reinvited_within_one_hour() throws Exception {

        UserProfileCreationData pendingUserData = createUserProfileData();
        UserProfileCreationResponse pendingUserResource = createActiveUserProfile(pendingUserData);

        UserProfileCreationData data = createUserProfileDataWithReInvite();
        data.setEmail(pendingUserData.getEmail());

        testRequestHandler.sendPost(
                testRequestHandler.asJsonString(data),
                HttpStatus.TOO_MANY_REQUESTS,
                requestUri);

    }



}
