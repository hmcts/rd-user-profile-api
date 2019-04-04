package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

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

        UserProfile userProfile =
            new UserProfile("test-idam-id", "joe.bloggs@somewhere.com", "joe", "bloggs");
        UserProfileResource expected = new UserProfileResource(userProfile);

        when(userProfileCreator.create(userProfileData)).thenReturn(userProfile);

        UserProfileResource resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        verify(userProfileCreator).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile =
            new UserProfile("test-idam-id", "joe.bloggs@somewhere.com", "joe", "bloggs");
        UserProfileResource expected = new UserProfileResource(userProfile);

        when(userProfileRetriever.retrieve(identifier)).thenReturn(userProfile);

        UserProfileResource resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        verify(userProfileRetriever).retrieve(any(UserProfileIdentifier.class));

    }

}
