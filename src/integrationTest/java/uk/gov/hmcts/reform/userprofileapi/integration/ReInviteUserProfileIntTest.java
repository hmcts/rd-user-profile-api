package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.TOO_MANY_REQUESTS;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.Optional;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@SpringBootTest(webEnvironment = MOCK)
@RunWith(SpringIntegrationSerenityRunner.class)
public class ReInviteUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    UserProfileCreationData pendingUserRequest = null;
    UserProfile userProfile = null;
    @Value("${resendInterval}")
    private String resendInterval;

    @Value("${syncInterval}")
    String syncInterval;

    @Before
    public void setUp() throws Exception {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        pendingUserRequest = buildCreateUserProfileData();
        createUser(pendingUserRequest, CREATED, UserProfileCreationResponse.class);

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByEmail(pendingUserRequest.getEmail().toLowerCase());
        userProfile = persistedUserProfile.get();

    }

    // AC1: resend invite to a given user
    @Test
    public void should_return_201_when_user_reinvited() throws Exception {

        userProfile.setLastUpdated(userProfile.getLastUpdated().minusHours(2L));
        userProfileRepository.save(userProfile);

        UserProfileCreationData data = buildCreateUserProfileData(true);
        data.setEmail(pendingUserRequest.getEmail());
        UserProfileCreationResponse reInvitedUserResponse = (UserProfileCreationResponse) createUser(data, CREATED, UserProfileCreationResponse.class);
        assertThat(reInvitedUserResponse.getIdamId()).isEqualTo(userProfile.getIdamId());
        verifyUserProfileCreation(reInvitedUserResponse, CREATED, data);
    }

    // AC3: resend invite to a given user who does not exist
    @Test
    public void should_return_404_when_user_doesnt_exists() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData(true);
        ErrorResponse errorResponse = (ErrorResponse) createUser(data, NOT_FOUND, ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage()).isEqualTo("4 : Resource not found");
        assertThat(errorResponse.getErrorDescription()).contains("could not find user profile");
    }

    // AC4: resend invite to a given user who is not in the 'Pending' state
    @Test
    public void should_return_400_when_user_reinvited_is_not_pending() throws Exception {

        userProfile.setLastUpdated(userProfile.getLastUpdated().minusHours(2L));
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfileRepository.save(userProfile);

        UserProfileCreationData data = buildCreateUserProfileData(true);
        data.setEmail(pendingUserRequest.getEmail());
        ErrorResponse errorResponse = (ErrorResponse) createUser(data, BAD_REQUEST, ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage()).isEqualTo("3 : There is a problem with your request. Please check and try again");
        assertThat(errorResponse.getErrorDescription()).isEqualTo("User is not in PENDING state");

    }

    // AC8: resend invite to a given user who was last invited less than 1 hour before
    @Test
    public void should_return_429_when_user_reinvited_within_one_hour() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData(true);
        data.setEmail(pendingUserRequest.getEmail());
        ErrorResponse errorResponse = (ErrorResponse) createUser(data, TOO_MANY_REQUESTS, ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(String.format("10 : The request was last made less than %s minutes ago. Please try after some time", resendInterval));
        assertThat(errorResponse.getErrorDescription()).contains(String.format("The request was last made less than %s minutes ago. Please try after some time", resendInterval));

    }

    // AC9: invited more than an hour ago but has recently activated their account
    @Test
    public void should_return_409_when_reinvited_user_gets_active_in_sidam_but_pending_in_up() throws Exception {

        userProfile.setLastUpdated(userProfile.getLastUpdated().minusHours(2L));
        userProfileRepository.save(userProfile);
        setSidamRegistrationMockWithStatus(HttpStatus.CONFLICT.value());

        UserProfileCreationData data = buildCreateUserProfileData(true);
        data.setEmail(pendingUserRequest.getEmail());
        ErrorResponse errorResponse = (ErrorResponse) createUser(data, HttpStatus.CONFLICT, ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage()).isEqualTo(String.format("7 : Resend invite failed as user is already active. Wait for %s minutes for the system to refresh.", syncInterval));
        assertThat(errorResponse.getErrorDescription()).contains(String.format("Resend invite failed as user is already active. Wait for %s minutes for the system to refresh.", syncInterval));
    }

}
