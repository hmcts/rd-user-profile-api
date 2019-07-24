package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileCreator;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileService;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileCreator userProfileCreator;

    @Mock
    private UserProfileRetriever userProfileRetriever;

    @InjectMocks
    private UserProfileService<RequestData> userProfileService;

    @Test
    public void should_call_creator_create_method_successfully() {

        CreateUserProfileData userProfileData = Mockito.mock(CreateUserProfileData.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        CreateUserProfileResponse expected = new CreateUserProfileResponse(userProfile);

        Mockito.when(userProfileCreator.create(userProfileData)).thenReturn(userProfile);

        CreateUserProfileResponse resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        Mockito.verify(userProfileCreator).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = Mockito.mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileResponse expected = new GetUserProfileResponse(userProfile);

        Mockito.when(userProfileRetriever.retrieve(identifier, false)).thenReturn(userProfile);

        GetUserProfileResponse resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void should_call_retriever_retrieve_with_roles_method_successfully() {
        UserProfileIdentifier identifier = Mockito.mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expected = new GetUserProfileWithRolesResponse(userProfile);

        Mockito.when(userProfileRetriever.retrieve(identifier, true)).thenReturn(userProfile);

        GetUserProfileWithRolesResponse resource = userProfileService.retrieveWithRoles(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

}
