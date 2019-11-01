package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileCreator;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

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

        userProfileService.update(updateUserProfileData, "1234", "EXUI");

        UserProfileRolesResponse userProfileResponse = mock(UserProfileRolesResponse.class);

        when(resourceUpdator.updateRoles(updateUserProfileData, "1234")).thenReturn(userProfileResponse);
        userProfileResponse = userProfileService.updateRoles(updateUserProfileData, "1234");

        assertThat(userProfileResponse).isNotNull();
    }

    @Test
    @Ignore
    public void testUpdate() {
        assertThat(userProfileService.update(null,null,null)).isInstanceOf(AttributeResponse.class);
    }

    @Test
    public void should_call_creator_create_method_successfully() {

        UserProfileCreationData userProfileData = mock(UserProfileCreationData.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileCreationResponse expected = new UserProfileCreationResponse(userProfile);

        when(userProfileCreator.create(userProfileData)).thenReturn(userProfile);

        UserProfileCreationResponse resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        Mockito.verify(userProfileCreator).create(any(UserProfileCreationData.class));

    }

    @Test
    public void should_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileResponse expected = new UserProfileResponse(userProfile);

        when(userProfileRetriever.retrieve(identifier, false)).thenReturn(userProfile);

        UserProfileResponse resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

    @Test
        public void should_call_retriever_retrieve_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileWithRolesResponse expected = new UserProfileWithRolesResponse(userProfile, true);

        when(userProfileRetriever.retrieve(identifier, true)).thenReturn(userProfile);

        UserProfileWithRolesResponse resource = userProfileService.retrieveWithRoles(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void should_call_retriever_retrieve_multiple_users_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        List<UserProfile> profileList = new ArrayList<>();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        profileList.add(userProfile);
        UserProfileWithRolesResponse expected = new UserProfileWithRolesResponse(userProfile, true);

        when(userProfileRetriever.retrieveMultipleProfiles(identifier, true, true)).thenReturn(profileList);

        UserProfileDataResponse resource = userProfileService.retrieveWithRoles(identifier, true, true);

        assertThat(resource).isNotNull();

    }
}
