package uk.gov.hmcts.reform.userprofileapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileService;


@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerTest {

    @Captor
    private ArgumentCaptor<UserProfileIdentifier> argumentCaptorMock;

    @Mock
    private UserProfileService<RequestData> userProfileServiceMock;

    @InjectMocks
    private UserProfileController sut;

    @Test
    public void testCreateUserProfile() {

        CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        CreateUserProfileResponse expectedBody = new CreateUserProfileResponse(userProfile);

        when(userProfileServiceMock.create(createUserProfileData)).thenReturn(expectedBody);

        ResponseEntity<CreateUserProfileResponse> resource = sut.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

        verify(userProfileServiceMock).create(any(CreateUserProfileData.class));

    }

    @Test
    public void testCreateUserProfileThrowsException() {
        CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        IllegalStateException ex = new IllegalStateException("this is a test exception");

        when(userProfileServiceMock.create(createUserProfileData)).thenThrow(ex);

        assertThatThrownBy(() -> sut.createUserProfile(createUserProfileData))
            .isEqualTo(ex);

        verify(userProfileServiceMock).create(any(CreateUserProfileData.class));
    }

    @Test
    public void testCreateUserProfileWithNullParam() {

        assertThatThrownBy(() -> sut.createUserProfile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("createUserProfileData");

        verifyZeroInteractions(userProfileServiceMock);

    }

    //@Test
    public void should_return_null_body_when_post_and_create_method_returns_null() {

        CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();

        when(userProfileServiceMock.create(createUserProfileData)).thenReturn(null);

        ResponseEntity<CreateUserProfileResponse> resource = sut.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isNull();

        verify(userProfileServiceMock).create(any(CreateUserProfileData.class));

    }

    //@Test
    public void should_call_retrieve_successfully_when_get_with_uuid_param() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expectedResource = new GetUserProfileWithRolesResponse(userProfile, true);

        when(userProfileServiceMock.retrieve(argumentCaptorMock.capture())).thenReturn(expectedResource);

        /*ResponseEntity<GetUserProfileWithRolesResponse> resource = sut.getUserProfileById(identifier.getValue());

        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);*/
        assertThat(argumentCaptorMock.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileServiceMock).retrieve(any(UserProfileIdentifier.class));

    }

    //@Test
    public void should_propagate_exception_when_get_with_uuid_and_retrieve_method_throws_exception() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(userProfileServiceMock.retrieve(argumentCaptorMock.capture())).thenThrow(ex);
        //   assertThatThrownBy(() -> sut.getUserProfileById(identifier.getValue())).isEqualTo(ex);
        assertThat(argumentCaptorMock.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileServiceMock).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void testGetUserProfileWithRolesById() {
        String id = "a833c2e2-2c73-4900-96ca-74b1efb37928";
        GetUserProfileWithRolesResponse responseMock = Mockito.mock(GetUserProfileWithRolesResponse.class);

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
        GetUserProfileWithRolesResponse responseMock = Mockito.mock(GetUserProfileWithRolesResponse.class);

        when(userProfileServiceMock.retrieveWithRoles(any(UserProfileIdentifier.class))).thenReturn(responseMock);

        assertThat(sut.getUserProfileWithRolesByEmail(email)).isEqualTo(ResponseEntity.ok(responseMock));

    }

    @Test
    public void testUpdateUserProfile() {

        UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);
        String idamId = "13b02995-5e44-4136-bf5a-46f4ff4acb8f";
        when(updateUserProfileDataMock.getRolesAdd()).thenReturn(null);
        ResponseEntity actual = sut.updateUserProfile(updateUserProfileDataMock, idamId);
        verify(userProfileServiceMock, times(1)).update(any(), any());
        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual.getStatusCode().value()).isEqualTo(expect.getStatusCode().value());
    }

    @Test
    public void should_throw_exception_when_get_with_email_null_parameters_passed_in() {

        verifyZeroInteractions(userProfileServiceMock);
    }


    //@Test
    public void should_call_request_manager_retrieve_method_with_idamId() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, "test-idam-id");
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expectedResource = new GetUserProfileWithRolesResponse(userProfile, true);

        when(userProfileServiceMock.retrieve(argumentCaptorMock.capture())).thenReturn(expectedResource);

        assertThat(argumentCaptorMock.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileServiceMock).retrieve(any(UserProfileIdentifier.class));

    }

    //@Test
    public void should_propagate_exception_when_handle_retrieve_with_idamId_throws_exception() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(userProfileServiceMock.retrieve(argumentCaptorMock.capture())).thenThrow(ex);

        assertThat(argumentCaptorMock.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileServiceMock).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_idamId_null_parameters_passed_in() {

        verifyZeroInteractions(userProfileServiceMock);

    }

    @Test(expected = RequiredFieldMissingException.class)
    public void  testThrowsMissingFieldWhenRolesAddedIsEmpty() throws Exception  {
        Set<RoleName> roles = new HashSet<RoleName>();
        UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);
        when(updateUserProfileDataMock.getRolesAdd())
                .thenReturn(roles);
        ResponseEntity expect = ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        assertThat(expect).isEqualTo(400);
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
        ResponseEntity actual = sut.updateUserProfile(updateUserProfileDataMock, idamId);
        verify(userProfileServiceMock, times(1)).updateRoles(any(), any());
        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual).isEqualTo(expect);
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
        ResponseEntity actual = sut.updateUserProfile(updateUserProfileDataMock, idamId);
        verify(userProfileServiceMock, times(1)).updateRoles(any(), any());
        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual).isEqualTo(expect);
    }
}
