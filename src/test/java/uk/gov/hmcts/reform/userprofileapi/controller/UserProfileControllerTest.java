package uk.gov.hmcts.reform.userprofileapi.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildUpdateUserProfileData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;




@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerTest {

    @Mock
    private UserProfileService<RequestData> userProfileServiceMock;

    @InjectMocks
    private UserProfileController sut;

    HttpServletRequest httpRequest = mock(HttpServletRequest.class);

    private static final String ORIGIN = "EXUI";

    @Test
    public void test_CreateUserProfile() {

        UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileCreationResponse expectedBody = new UserProfileCreationResponse(userProfile);

        when(userProfileServiceMock.create(userProfileCreationData)).thenReturn(expectedBody);

        ResponseEntity<UserProfileCreationResponse> resource = sut.createUserProfile(userProfileCreationData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

        verify(userProfileServiceMock, times(1)).create(any(UserProfileCreationData.class));
        verify(userProfileServiceMock, times(0))
                .reInviteUser(any(UserProfileCreationData.class));
    }

    @Test
    public void test_ReInviteUserProfile() {

        UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
                .buildCreateUserProfileData(true);
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        UserProfileCreationResponse expectedBody = new UserProfileCreationResponse(userProfile);

        when(userProfileServiceMock.reInviteUser(userProfileCreationData)).thenReturn(expectedBody);

        ResponseEntity<UserProfileCreationResponse> resource = sut.createUserProfile(userProfileCreationData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

        verify(userProfileServiceMock, times(0)).create(any(UserProfileCreationData.class));
        verify(userProfileServiceMock, times(1))
                .reInviteUser(any(UserProfileCreationData.class));
    }

    @Test
    public void test_CreateUserProfileThrowsException() {
        UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();
        IllegalStateException ex = new IllegalStateException("this is a test exception");

        when(userProfileServiceMock.create(userProfileCreationData)).thenThrow(ex);

        assertThatThrownBy(() -> sut.createUserProfile(userProfileCreationData)).isEqualTo(ex);

        verify(userProfileServiceMock, times(1)).create(any(UserProfileCreationData.class));
    }

    @Test
    public void test_CreateUserProfileWithNullParam() {
        assertThatThrownBy(() -> sut.createUserProfile(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("createUserProfileData");

        verifyNoInteractions(userProfileServiceMock);
    }

    @Test
    public void test_GetUserProfileWithRolesById() {
        String id = "a833c2e2-2c73-4900-96ca-74b1efb37928";
        UserProfileWithRolesResponse responseMock = Mockito.mock(UserProfileWithRolesResponse.class);

        when(userProfileServiceMock.retrieveWithRoles(any(UserProfileIdentifier.class))).thenReturn(responseMock);

        assertThat(sut.getUserProfileWithRolesById(id)).isEqualTo(ResponseEntity.ok(responseMock));
        verify(userProfileServiceMock, times(1))
                .retrieveWithRoles(any(UserProfileIdentifier.class));
    }

    @Test
    public void test_throw_exception_when_get_with_uuid_null_parameters_passed_in() {
        verifyNoInteractions(userProfileServiceMock);
    }


    @Test
    public void test_GetUserProfileWithRolesByEmail() {

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn(" ");
        UserProfileWithRolesResponse responseMock = Mockito.mock(UserProfileWithRolesResponse.class);
        when(userProfileServiceMock.retrieveWithRoles(any(UserProfileIdentifier.class))).thenReturn(responseMock);
        String email = "test@test.com";
        assertThat(sut.getUserProfileWithRolesByEmail(email)).isEqualTo(ResponseEntity.ok(responseMock));
        verify(userProfileServiceMock, times(1))
                .retrieveWithRoles(any(UserProfileIdentifier.class));
        verify(httpRequest, times(2)).getHeader(anyString());
    }

    @Test
    public void test_GetUserProfileWithRolesByEmailByHeader() {

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        UserProfileWithRolesResponse responseMock = Mockito.mock(UserProfileWithRolesResponse.class);
        when(httpRequest.getHeader(anyString())).thenReturn("test@test.com");
        when(userProfileServiceMock.retrieveWithRoles(any(UserProfileIdentifier.class))).thenReturn(responseMock);
        String email = " ";
        assertThat(sut.getUserProfileWithRolesByEmail(email)).isEqualTo(ResponseEntity.ok(responseMock));
        verify(userProfileServiceMock, times(1))
                .retrieveWithRoles(any(UserProfileIdentifier.class));
        verify(httpRequest, times(2)).getHeader(anyString());
    }

    @Test
    public void test_UpdateUserProfile() {
        UpdateUserProfileData updateUserProfileData = buildUpdateUserProfileData();
        AttributeResponse attributeResponse = new AttributeResponse(status(OK).build());

        when(userProfileServiceMock.update(any(), any(), any())).thenReturn(attributeResponse);

        ResponseEntity actual = sut.updateUserProfile(updateUserProfileData, UUID.randomUUID().toString(), ORIGIN);
        verify(userProfileServiceMock, times(1)).update(any(), any(), any());

        ResponseEntity expect = status(OK).build();
        assertThat(actual.getStatusCode().value()).isEqualTo(expect.getStatusCode().value());
    }


    @Test
    public void test_throw_exception_when_get_with_idamId_null_parameters_passed_in() {
        verifyNoInteractions(userProfileServiceMock);
    }

    @Test
    public void test_UpdateUserProfileRoles() {
        UpdateUserProfileData updateUserProfileData = buildUpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(new RoleName("pui-case-manager"));
        roles.add(new RoleName("pui-case-organisation"));
        updateUserProfileData.setRolesAdd(roles);

        ResponseEntity actual = sut.updateUserProfile(updateUserProfileData, UUID.randomUUID().toString(), ORIGIN);
        verify(userProfileServiceMock, times(1)).updateRoles(any(), any());

        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual).isEqualTo(expect);

    }

    @Test
    public void test_retrieveUserProfiles() {
        List<String> userIds = Arrays.asList("1", "2");
        UserProfileDataRequest userProfileDataRequest = new UserProfileDataRequest(userIds);

        ResponseEntity<UserProfileDataResponse> responseEntity = sut.retrieveUserProfiles("false",
                "true", userProfileDataRequest);
        assertThat(responseEntity).isNotNull();

        verify(userProfileServiceMock, times(1))
                .retrieveWithRoles(any(UserProfileIdentifier.class), any(Boolean.class), any(Boolean.class));
    }

    @Test
    public void test_UpdateUserProfileRolesForDelete() {
        UpdateUserProfileData updateUserProfileData = buildUpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(new RoleName("pui-case-manager"));
        roles.add(new RoleName("pui-case-organisation"));
        updateUserProfileData.setRolesDelete(roles);

        ResponseEntity actual = sut.updateUserProfile(updateUserProfileData, UUID.randomUUID().toString(), ORIGIN);
        verify(userProfileServiceMock, times(1)).updateRoles(any(), any());

        ResponseEntity expect = ResponseEntity.status(HttpStatus.OK).build();
        assertThat(actual).isEqualTo(expect);
    }

    @Test
    public void testDeleteUserProfiles() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        List<String> userIds = new ArrayList<>();
        userIds.add(userProfile.getIdamId());
        UserProfileDataRequest userProfileDataRequest = new UserProfileDataRequest(userIds);
        UserProfilesDeletionResponse userProfilesDeletionResponse = new UserProfilesDeletionResponse(204,
                "UserProfiles Successfully Deleted");

        when(userProfileServiceMock.delete(any(UserProfileDataRequest.class)))
                .thenReturn(userProfilesDeletionResponse);
        ResponseEntity<UserProfilesDeletionResponse> responseEntityActual = sut
                .deleteUserProfiles(userProfileDataRequest);
        assertThat(responseEntityActual).isNotNull();

        verify(userProfileServiceMock, times(1)).delete(any(UserProfileDataRequest.class));
        assertThat(responseEntityActual.getStatusCodeValue()).isEqualTo(204);
        assertThat(responseEntityActual.getBody().getMessage()).isEqualTo("UserProfiles Successfully Deleted");
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testDeleteUserProfilesWithEmptyUserIdInTheRequest() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        List<String> userIds = new ArrayList<>();
        userIds.add("");
        UserProfileDataRequest userProfileDataRequest = new UserProfileDataRequest(userIds);
        sut.deleteUserProfiles(userProfileDataRequest);
        verify(userProfileServiceMock, times(0)).delete(any(UserProfileDataRequest.class));

    }
}
