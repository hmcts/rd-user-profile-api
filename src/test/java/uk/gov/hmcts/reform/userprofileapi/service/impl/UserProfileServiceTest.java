package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

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
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfilesDeletionData;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceDeleter;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileCreator userProfileCreator;

    @Mock
    private UserProfileRetriever userProfileRetriever;

    @Mock
    private ResourceUpdator<UpdateUserProfileData> resourceUpdatorMock;

    @InjectMocks
    private UserProfileService<RequestData> userProfileService;

    @Mock
    private ResourceDeleter<UserProfilesDeletionData> userProfileDeleterMock;

    @Test
    public void testUpdateRoles() {
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(new RoleName("pui-case-manager"));
        roles.add(new RoleName("pui-case-organisation"));
        updateUserProfileData.setRolesAdd(roles);

        userProfileService.update(updateUserProfileData, "1234", "EXUI");

        UserProfileRolesResponse userProfileResponse = mock(UserProfileRolesResponse.class);

        when(resourceUpdatorMock.updateRoles(updateUserProfileData, "1234")).thenReturn(userProfileResponse);
        userProfileResponse = userProfileService.updateRoles(updateUserProfileData, "1234");

        assertThat(userProfileResponse).isNotNull();
        verify(resourceUpdatorMock, Mockito.times(1)).updateRoles(any(), any(String.class));
    }

    @Test
    public void testUpdate() {
        AttributeResponse attributeResponseMock = Mockito.mock(AttributeResponse.class);
        when(resourceUpdatorMock.update(any(), any(), any())).thenReturn(attributeResponseMock);

        assertThat(userProfileService.update(null, null, null)).isInstanceOf(AttributeResponse.class);

        verify(resourceUpdatorMock, times(1)).update(any(), any(), any());
    }

    @Test
    public void testShould_call_creator_create_method_successfully() {
        UserProfileCreationData userProfileData = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileCreationResponse expected = new UserProfileCreationResponse(userProfile);

        when(userProfileCreator.create(userProfileData)).thenReturn(userProfile);

        UserProfileCreationResponse resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        verify(userProfileCreator).create(any(UserProfileCreationData.class));

    }

    @Test
    public void testShould_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileResponse expected = new UserProfileResponse(userProfile);

        when(userProfileRetriever.retrieve(identifier, false)).thenReturn(userProfile);

        UserProfileResponse resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

        verify(userProfileRetriever, Mockito.times(1)).retrieve(any(), any(boolean.class));

    }

    @Test
    public void testShould_call_retriever_retrieve_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileWithRolesResponse expected = new UserProfileWithRolesResponse(userProfile, true);

        when(userProfileRetriever.retrieve(identifier, true)).thenReturn(userProfile);

        UserProfileWithRolesResponse resource = userProfileService.retrieveWithRoles(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);

        verify(userProfileRetriever, Mockito.times(1)).retrieve(any(), any(boolean.class));

    }

    @Test
    public void testShould_call_retriever_retrieve_multiple_users_with_roles_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        List<UserProfile> profileList = new ArrayList<>();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        profileList.add(userProfile);

        when(userProfileRetriever.retrieveMultipleProfiles(identifier, true, true)).thenReturn(profileList);

        UserProfileDataResponse resource = userProfileService.retrieveWithRoles(identifier, true, true);

        assertThat(resource).isNotNull();

        verify(userProfileRetriever, Mockito.times(1)).retrieveMultipleProfiles(any(), any(boolean.class), any(boolean.class));
    }


    @Test
    public void testDeleteUserProfiles_successfully() {

        UserProfilesDeletionData deletionData = mock(UserProfilesDeletionData.class);
        final UserProfilesDeletionResponse userProfilesDeletionResponse = new UserProfilesDeletionResponse(204, "successfully deleted");

        when(userProfileDeleterMock.delete(deletionData)).thenReturn(userProfilesDeletionResponse);

        UserProfilesDeletionResponse userProfilesDelResponse = userProfileService.delete(deletionData);

        assertThat(userProfilesDelResponse).isNotNull();
        assertThat(userProfilesDelResponse.getStatusCode()).isEqualTo(204);
        assertThat(userProfilesDelResponse.getMessage()).isEqualTo("successfully deleted");
        verify(userProfileDeleterMock, Mockito.times(1)).delete(any());
    }
}
