package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
public class ReInviteUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    UserProfileCreationData pendingUserRequest = null;
    UserProfile userProfile = null;

    @Before
    public void setUp() throws Exception {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        pendingUserRequest = buildCreateUserProfileData();
        UserProfileCreationResponse pendingUserResponse = (UserProfileCreationResponse) createUser(pendingUserRequest, CREATED, UserProfileCreationResponse.class);

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByIdamId(pendingUserResponse.getIdamId());
        userProfile = persistedUserProfile.get();
        userProfile.setLastUpdated(userProfile.getLastUpdated().minusHours(2L));
        userProfileRepository.save(userProfile);
    }

    // AC1: resend invite to a given user
    @Test
    public void should_return_201_when_user_reinvited() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData(true);
        data.setEmail(pendingUserRequest.getEmail());
        UserProfileCreationResponse reInvitedUserResponse = (UserProfileCreationResponse) createUser(data, CREATED, UserProfileCreationResponse.class);
        verifyUserProfileCreation(reInvitedUserResponse, CREATED, data);
    }

    // AC9: invited more than an hour ago but has recently activated their account
    @Test
    public void should_return_409_when_reinvited_user_gets_active_in_sidam_but_pending_in_up() throws Exception {

        setSidamRegistrationMockWithStatus(409);

        UserProfileCreationData data = buildCreateUserProfileData(true);
        data.setEmail(pendingUserRequest.getEmail());
        ErrorResponse errorResponse = (ErrorResponse) createUser(data, HttpStatus.CONFLICT, ErrorResponse.class);
        assertThat(errorResponse).isNotNull();
    }

}
