package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileCreator;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileService;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileCreator userProfileCreator;

    @Mock
    private UserProfileRetriever userProfileRetriever;

    @Mock
    private ResourceUpdator<UpdateUserProfileData> resourceUpdator;

    @InjectMocks
    private UserProfileService<RequestData> userProfileService;

    @Test
    public void testUpdateRoles() {

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<>();
        roles.add(roleName1);
        roles.add(roleName2);

        updateUserProfileData.setRolesAdd(roles);

        userProfileService.update(updateUserProfileData, "1234");

        UserProfileRolesResponse userProfileRolesResponse = mock(UserProfileRolesResponse.class);

        when(resourceUpdator.updateRoles(updateUserProfileData, "1234")).thenReturn(userProfileRolesResponse);
        userProfileRolesResponse = userProfileService.updateRoles(updateUserProfileData, "1234");

        assertThat(userProfileRolesResponse).isNotNull();
    }

    @Test
    public void should_call_creator_create_method_successfully() {

        CreateUserProfileData userProfileData = mock(CreateUserProfileData.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        CreateUserProfileResponse expected = new CreateUserProfileResponse(userProfile);

        when(userProfileCreator.create(userProfileData)).thenReturn(userProfile);

        CreateUserProfileResponse resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        Mockito.verify(userProfileCreator).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileResponse expected = new GetUserProfileResponse(userProfile);

        when(userProfileRetriever.retrieve(identifier, false)).thenReturn(userProfile);

        GetUserProfileResponse resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

    @Test
        public void should_call_retriever_retrieve_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expected = new GetUserProfileWithRolesResponse(userProfile, true);

        when(userProfileRetriever.retrieve(identifier, true)).thenReturn(userProfile);

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

        when(userProfileRetriever.retrieveMultipleProfiles(identifier, true, true)).thenReturn(profileList);

        GetUserProfilesResponse resource = userProfileService.retrieveWithRoles(identifier, true, true);

        assertThat(resource).isNotNull();

    }
}
