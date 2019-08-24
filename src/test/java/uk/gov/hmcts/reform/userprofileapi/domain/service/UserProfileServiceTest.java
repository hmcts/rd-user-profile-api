package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.client.*;
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

        CreateUserProfileData userProfileData = mock(CreateUserProfileData.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        CreateUserProfileResponse expected = new CreateUserProfileResponse(userProfile);

        Mockito.when(userProfileCreator.create(userProfileData)).thenReturn(userProfile);

        CreateUserProfileResponse resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        Mockito.verify(userProfileCreator).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileResponse expected = new GetUserProfileResponse(userProfile);

        Mockito.when(userProfileRetriever.retrieve(identifier, false)).thenReturn(userProfile);

        GetUserProfileResponse resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

    @Test
        public void should_call_retriever_retrieve_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expected = new GetUserProfileWithRolesResponse(userProfile, true);

        Mockito.when(userProfileRetriever.retrieve(identifier, true)).thenReturn(userProfile);

        GetUserProfileWithRolesResponse resource = userProfileService.retrieveWithRoles(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void should_call_retriever_retrieve_multiple_users_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        List<UserProfile> profileList = new ArrayList<>();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        profileList.add(userProfile);
        GetUserProfileWithRolesResponse expected = new GetUserProfileWithRolesResponse(userProfile, true);

        Mockito.when(userProfileRetriever.retrieveMultipleProfiles(identifier, true, true)).thenReturn(profileList);

        GetUserProfilesResponse resource = userProfileService.retrieveWithRoles(identifier, true, true);

        assertThat(resource).isNotNull();

    }



}
