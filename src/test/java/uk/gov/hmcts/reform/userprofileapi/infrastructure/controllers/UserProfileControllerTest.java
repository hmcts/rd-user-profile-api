package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.domain.service.RequestManager;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerTest {

    //TODO Add more tests for all endpoints and permutations

    @Mock
    private RequestManager requestManager;

    @InjectMocks
    private UserProfileController userProfileController;

    @Test
    public void should_call_request_manager_with_correct_parameters() {

        CreateUserProfileData createUserProfileData = new CreateUserProfileData("test@somewhere.com", "jane", "doe");
        UserProfileResource expectedBody = new UserProfileResource(UUID.randomUUID(), "test-idamId", "jane", "doe");

        when(requestManager.handleCreate(createUserProfileData)).thenReturn(expectedBody);

        ResponseEntity<UserProfileResource> resource = userProfileController.createUserProfile(createUserProfileData);
        assertThat(resource.getBody()).isEqualToComparingFieldByField(expectedBody);

    }


}
