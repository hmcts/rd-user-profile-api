package uk.gov.hmcts.reform.userprofileapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;


@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerTest {

    @Captor
    private ArgumentCaptor<UserProfileIdentifier> argumentCaptorMock;

    @Mock
    private UserProfileService<RequestData> userProfileServiceMock;

    @InjectMocks
    private UserProfileController sut;

    private static final String ORIGIN  = "EXUI";

    @Test
    public void testCreateUserProfile() {

        UserProfileCreationData userProfileCreationData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileCreationResponse expectedBody = new UserProfileCreationResponse(userProfile);

        when(userProfileServiceMock.create(userProfileCreationData)).thenReturn(expectedBody);

        ResponseEntity<UserProfileCreationResponse> resource = sut.createUserProfile(userProfileCreationData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

        verify(userProfileServiceMock).create(any(UserProfileCreationData.class));

    }

    @Test
    public void testCreateUserProfileThrowsException() {
        UserProfileCreationData userProfileCreationData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        IllegalStateException ex = new IllegalStateException("this is a test exception");

        when(userProfileServiceMock.create(userProfileCreationData)).thenThrow(ex);

        assertThatThrownBy(() -> sut.createUserProfile(userProfileCreationData))
                .isEqualTo(ex);

        verify(userProfileServiceMock).create(any(UserProfileCreationData.class));
    }

    @Test
    public void testCreateUserProfileWithNullParam() {

        assertThatThrownBy(() -> sut.createUserProfile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("createUserProfileData");

        verifyZeroInteractions(userProfileServiceMock);

    }

    @Test
    public void testGetUserProfileWithRolesById() {
        String id = "a833c2e2-2c73-4900-96ca-74b1efb37928";
        UserProfileWithRolesResponse responseMock = Mockito.mock(UserProfileWithRolesResponse.class);

        when(userProfileServiceMock.retrieveWithRoles(any(UserProfileIdentifier.class))).thenReturn(responseMock);

        assertThat(sut.getUserProfileWithRolesById(id)).isEqualTo(ResponseEntity.ok(responseMock));
    }

    @Test
    public void should_throw_exception_when_get_with_uuid_null_parameters_passed_in() {
        verifyZeroInteractions(userProfileServiceMock);
    }


    @Test
    public void testGetUserProfileWithRolesByEmail() {
        String email = "test@test.com";
        UserProfileWithRolesResponse responseMock = Mockito.mock(UserProfileWithRolesResponse.class);

        when(userProfileServiceMock.retrieveWithRoles(any(UserProfileIdentifier.class))).thenReturn(responseMock);

        assertThat(sut.getUserProfileWithRolesByEmail(email)).isEqualTo(ResponseEntity.ok(responseMock));

    }

    @Test
    public void testUpdateUserProfile() {

        UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);
        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        ResponseEntity responseEntityMock = Mockito.mock(ResponseEntity.class);

        String idamId = "13b02995-5e44-4136-bf5a-46f4ff4acb8f";

        when(updateUserProfileDataMock.getRolesAdd()).thenReturn(null);
        when(updateUserProfileDataMock.getRolesDelete()).thenReturn(null);
        ResponseEntity actual = sut.updateUserProfile(updateUserProfileDataMock, idamId, ORIGIN);

        verify(userProfileServiceMock, times(1)).update(any(), any());
        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual.getStatusCode().value()).isEqualTo(expect.getStatusCode().value());
    }


    @Test
    public void should_throw_exception_when_get_with_idamId_null_parameters_passed_in() {

        verifyZeroInteractions(userProfileServiceMock);
    }

    @Test
    public void testUpdateUserProfileRoles() {
        UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);

        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<RoleName>();
        roles.add(roleName1);
        roles.add(roleName2);
        when(updateUserProfileDataMock.getRolesAdd()).thenReturn(roles);
        String idamId = "13b02995-5e44-4136-bf5a-46f4ff4acb8f";
        ResponseEntity actual = sut.updateUserProfile(updateUserProfileDataMock, idamId, ORIGIN);
        verify(userProfileServiceMock, times(1)).updateRoles(any(), any());
        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual).isEqualTo(expect);

    }

    @Test
    public void testretrieveUserProfiles() {

        List<String> userIds = new ArrayList<>();
        userIds.add("1");
        userIds.add("2");
        UserProfileDataRequest userProfileDataRequest = mock(UserProfileDataRequest.class);
        when(userProfileDataRequest.getUserIds()).thenReturn(userIds);
        ResponseEntity<UserProfileDataResponse> responseEntity = sut.retrieveUserProfiles("false","true", userProfileDataRequest);
        assertThat(responseEntity).isNotNull();

    }

    @Test
    public void testUpdateUserProfileRolesForDelete() {
        UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<RoleName>();
        roles.add(roleName1);
        roles.add(roleName2);
        when(updateUserProfileDataMock.getRolesDelete()).thenReturn(roles);
        String idamId = "13b02995-5e44-4136-bf5a-46f4ff4acb8f";
        ResponseEntity actual = sut.updateUserProfile(updateUserProfileDataMock, idamId, ORIGIN);
        verify(userProfileServiceMock, times(1)).updateRoles(any(), any());
        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual).isEqualTo(expect);
    }
}
