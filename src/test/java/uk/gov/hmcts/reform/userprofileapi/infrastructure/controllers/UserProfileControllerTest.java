package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.domain.service.UserProfileService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerTest {

    @Captor
    private ArgumentCaptor<UserProfileIdentifier> identifierArgumentCaptor;

    @Mock
    private UserProfileService requestManager;

    @InjectMocks
    private UserProfileController userProfileController;

    @Test
    public void should_call_create_successfully_when_post_with_correct_data() {

        CreateUserProfileData createUserProfileData = new CreateUserProfileData("test@somewhere.com", "jane", "doe");
        UserProfileResource expectedBody = new UserProfileResource(UUID.randomUUID(), "test-idamId", "jane", "doe");

        when(requestManager.handleCreate(createUserProfileData)).thenReturn(expectedBody);

        ResponseEntity<UserProfileResource> resource = userProfileController.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

        verify(requestManager).handleCreate(any(CreateUserProfileData.class));

    }

    @Test
    public void should_propagate_exception_when_post_and_create_method_throws_exception() {

        CreateUserProfileData createUserProfileData = new CreateUserProfileData("test@somewhere.com", "jane", "doe");
        IllegalStateException ex = new IllegalStateException("this is a test exception");

        when(requestManager.handleCreate(createUserProfileData)).thenThrow(ex);

        assertThatThrownBy(() -> userProfileController.createUserProfile(createUserProfileData))
            .isEqualTo(ex);

        verify(requestManager).handleCreate(any(CreateUserProfileData.class));

    }

    @Test
    public void should_throw_exception_when_post_and_null_parameters_passed_in() {

        assertThatThrownBy(() -> userProfileController.createUserProfile(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("createUserProfileData");

        verifyZeroInteractions(requestManager);

    }

    @Test
    public void should_return_null_body_when_post_and_create_method_returns_null() {

        CreateUserProfileData createUserProfileData = new CreateUserProfileData("test@somewhere.com", "jane", "doe");

        when(requestManager.handleCreate(createUserProfileData)).thenReturn(null);

        ResponseEntity<UserProfileResource> resource = userProfileController.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isNull();

        verify(requestManager).handleCreate(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retrieve_successfully_when_get_with_uuid_param() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        UserProfileResource expectedResource = new UserProfileResource(UUID.randomUUID(), "test-idamId", "jane", "doe");

        when(requestManager.handleRetrieve(identifierArgumentCaptor.capture())).thenReturn(expectedResource);

        ResponseEntity<UserProfileResource> resource = userProfileController.getUserProfileById(identifier.getValue());

        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(requestManager).handleRetrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_propagate_exception_when_get_with_uuid_and_retrieve_method_throws_exception() {
        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.UUID, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(requestManager.handleRetrieve(identifierArgumentCaptor.capture())).thenThrow(ex);

        assertThatThrownBy(() -> userProfileController.getUserProfileById(identifier.getValue())).isEqualTo(ex);

        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(requestManager).handleRetrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_uuid_null_parameters_passed_in() {

        assertThatThrownBy(() -> userProfileController.getUserProfileById(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("uuid");

        verifyZeroInteractions(requestManager);

    }


    @Test
    public void should_call_retrieve_successfully_when_get_with_email_param() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.EMAIL, "test@email.com");
        UserProfileResource expectedResource = new UserProfileResource(UUID.randomUUID(), "test-idamId", "jane", "doe");

        when(requestManager.handleRetrieve(identifierArgumentCaptor.capture())).thenReturn(expectedResource);

        ResponseEntity<UserProfileResource> resource = userProfileController.getUserProfileByEmail(identifier.getValue());

        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(requestManager).handleRetrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_propagate_exception_when_get_with_email_and_retrieve_method_throws_exception() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.EMAIL, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(requestManager.handleRetrieve(identifierArgumentCaptor.capture())).thenThrow(ex);

        assertThatThrownBy(() -> userProfileController.getUserProfileByEmail(identifier.getValue())).isEqualTo(ex);

        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(requestManager).handleRetrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_email_null_parameters_passed_in() {

        assertThatThrownBy(() -> userProfileController.getUserProfileByEmail(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("email");

        verifyZeroInteractions(requestManager);

    }

    @Test
    public void should_call_request_manager_retrieve_method_with_idamId() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.IDAMID, "test-idam-id");
        UserProfileResource expectedResource = new UserProfileResource(UUID.randomUUID(), "test-idamId", "jane", "doe");

        when(requestManager.handleRetrieve(identifierArgumentCaptor.capture())).thenReturn(expectedResource);

        ResponseEntity<UserProfileResource> resource = userProfileController.getUserProfileByIdamId(identifier.getValue());

        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedResource);
        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(requestManager).handleRetrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_propagate_exception_when_handle_retrieve_with_idamId_throws_exception() {

        UserProfileIdentifier identifier = new UserProfileIdentifier(IdentifierName.IDAMID, UUID.randomUUID().toString());
        IllegalStateException ex = new IllegalStateException("This is a test exception");

        when(requestManager.handleRetrieve(identifierArgumentCaptor.capture())).thenThrow(ex);

        assertThatThrownBy(() -> userProfileController.getUserProfileByIdamId(identifier.getValue())).isEqualTo(ex);

        assertThat(identifierArgumentCaptor.getValue()).isEqualToComparingFieldByField(identifier);

        verify(requestManager).handleRetrieve(any(UserProfileIdentifier.class));

    }

    @Test
    public void should_throw_exception_when_get_with_idamId_null_parameters_passed_in() {

        assertThatThrownBy(() -> userProfileController.getUserProfileByIdamId(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("idamId");

        verifyZeroInteractions(requestManager);

    }


}
