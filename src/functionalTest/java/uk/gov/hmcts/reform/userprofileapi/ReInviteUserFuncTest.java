package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
public class ReInviteUserFuncTest extends AbstractFunctional {

    //AC3: resend invite to a given user who does not exist
    @Test
    public void should_return_404_when_user_reinvited_if_user_not_exists() throws Exception {

        ErrorResponse errorResponse = testRequestHandler.sendPost(
                testRequestHandler.asJsonString(createUserProfileDataWithReInvite()),
                HttpStatus.NOT_FOUND,
                requestUri).as(ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage()).isEqualTo("4 : Resource not found");
        assertThat(errorResponse.getErrorDescription()).contains("could not find user profile");

    }

    //AC4: resend invite to a given user who is not in the 'Pending' state
    @Test
    public void should_return_400_when_user_reinvited_if_user_is_active() throws Exception {

        UserProfileCreationData activeUserData = createUserProfileData();
        UserProfileCreationResponse duplicateUserResource = createActiveUserProfile(activeUserData);
        verifyCreateUserProfile(duplicateUserResource);

        UserProfileCreationData data = createUserProfileDataWithReInvite();
        data.setEmail(activeUserData.getEmail());

        ErrorResponse errorResponse = testRequestHandler.sendPost(
                testRequestHandler.asJsonString(data),
                HttpStatus.BAD_REQUEST,
                requestUri).as(ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("3 : There is a problem with your request. Please check and try again");
        assertThat(errorResponse.getErrorDescription()).isEqualTo("User is not in PENDING state");
    }

    //AC5: resend invite to a given user who was last invited less than one hour before
    @Test
    public void should_return_429_when_user_reinvited_within_one_hour() throws Exception {

        UserProfileCreationData pendingUserData = createUserProfileData();
        UserProfileCreationResponse pendingUserResource = createUserProfile(pendingUserData, HttpStatus.CREATED);

        UserProfileCreationData data = createUserProfileDataWithReInvite();
        data.setEmail(pendingUserData.getEmail());

        ErrorResponse errorResponse = testRequestHandler.sendPost(
                testRequestHandler.asJsonString(data),
                HttpStatus.TOO_MANY_REQUESTS,
                requestUri).as(ErrorResponse.class);

        assertThat(errorResponse.getErrorMessage()).isEqualTo(String.format(
                String.format("10 : The request was last made less than %s minutes ago. Please try after some time",
                        resendInterval)));
    }
}
