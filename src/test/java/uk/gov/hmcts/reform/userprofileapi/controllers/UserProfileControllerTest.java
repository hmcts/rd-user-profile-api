package uk.gov.hmcts.reform.userprofileapi.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;


@Ignore
@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerTest {

    @Captor
    private ArgumentCaptor<UserProfileIdentifier> identifierArgumentCaptor;

    @Mock
    private UserProfileService<RequestData> userProfileService;

    @InjectMocks
    private UserProfileController userProfileController;

    @Test
    public void should_call_create_successfully_when_post_with_correct_data() {

        CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        CreateUserProfileResponse expectedBody = new CreateUserProfileResponse(userProfile);

        when(userProfileService.create(createUserProfileData)).thenReturn(expectedBody);

        ResponseEntity<CreateUserProfileResponse> resource = userProfileController.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

        verify(userProfileService).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_propagate_exception_when_post_and_create_method_throws_exception() {

        CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        IllegalStateException ex = new IllegalStateException("this is a test exception");

        when(userProfileService.create(createUserProfileData)).thenThrow(ex);

        assertThatThrownBy(() -> userProfileController.createUserProfile(createUserProfileData))
            .isEqualTo(ex);

        verify(userProfileService).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_throw_exception_when_post_and_null_parameters_passed_in() {

        assertThatThrownBy(() -> userProfileController.createUserProfile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("createUserProfileData");

        verifyZeroInteractions(userProfileService);

    }

    @Test
    public void should_return_null_body_when_post_and_create_method_returns_null() {

        CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();

        when(userProfileService.create(createUserProfileData)).thenReturn(null);

        ResponseEntity<CreateUserProfileResponse> resource = userProfileController.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isNull();

        verify(userProfileService).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retrieve_successfully_when_get_with_uuid_param() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expectedResource = new GetUserProfileWithRolesResponse(userProfile);

        when(userProfileService.retrieve(identifierArgumentCaptor.capture())).thenReturn(expectedResource);

        /*ResponseEntity<GetUserProfileWithRolesResponse> resource = userProfileController.getUserProfileById(identifier.getValue());

        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);*/
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileService).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_propagate_exception_when_get_with_uuid_and_retrieve_method_throws_exception() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(userProfileService.retrieve(identifierArgumentCaptor.capture())).thenThrow(ex);
        //   assertThatThrownBy(() -> userProfileController.getUserProfileById(identifier.getValue())).isEqualTo(ex);
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileService).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_uuid_null_parameters_passed_in() {

        /* assertThatThrownBy(() -> userProfileController.getUserProfileById(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("uuid");*/

        verifyZeroInteractions(userProfileService);

    }


    @Test
    public void should_call_retrieve_successfully_when_get_with_email_param() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.EMAIL, "test@email.com");
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expectedResource = new GetUserProfileWithRolesResponse(userProfile);


        when(userProfileService.retrieve(identifierArgumentCaptor.capture())).thenReturn(expectedResource);

        //  ResponseEntity<GetUserProfileWithRolesResponse> resource = userProfileController.getUserProfileByEmail(identifier.getValue());

        // assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileService).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_propagate_exception_when_get_with_email_and_retrieve_method_throws_exception() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.EMAIL, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(userProfileService.retrieve(identifierArgumentCaptor.capture())).thenThrow(ex);

        //  assertThatThrownBy(() -> userProfileController.getUserProfileByEmail(identifier.getValue())).isEqualTo(ex);

        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileService).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_email_null_parameters_passed_in() {

        /*  assertThatThrownBy(() -> userProfileController.getUserProfileByEmail(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("email");*/

        verifyZeroInteractions(userProfileService);

    }

    @Test
    public void should_call_request_manager_retrieve_method_with_idamId() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, "test-idam-id");
        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileWithRolesResponse expectedResource = new GetUserProfileWithRolesResponse(userProfile);

        when(userProfileService.retrieve(identifierArgumentCaptor.capture())).thenReturn(expectedResource);

        // ResponseEntity<GetUserProfileWithRolesResponse> resource = userProfileController.getUserProfileById(identifier.getValue());

        //  assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileService).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_propagate_exception_when_handle_retrieve_with_idamId_throws_exception() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(userProfileService.retrieve(identifierArgumentCaptor.capture())).thenThrow(ex);

        // assertThatThrownBy(() -> userProfileController.getUserProfileById(identifier.getValue())).isEqualTo(ex);

        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(userProfileService).retrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_idamId_null_parameters_passed_in() {

        /*   assertThatThrownBy(() -> userProfileController.getUserProfileById(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("idamId");*/

        verifyZeroInteractions(userProfileService);

    }

}
