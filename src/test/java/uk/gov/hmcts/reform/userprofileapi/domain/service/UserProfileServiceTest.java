package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileCreatorImpl;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileRetrieverImpl;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileServiceTest {

    @Mock
    private UserProfileCreatorImpl userProfileCreatorImpl;

    @Mock
    private UserProfileRetrieverImpl userProfileRetrieverImpl;

    @InjectMocks
    private UserProfileService<RequestData> userProfileService;

    @Test
    public void should_call_creator_create_method_successfully() {

        CreateUserProfileData userProfileData = mock(CreateUserProfileData.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        CreateUserProfileResponse expected = new CreateUserProfileResponse(userProfile);

        when(userProfileCreatorImpl.create(userProfileData)).thenReturn(userProfile);

        CreateUserProfileResponse resource = userProfileService.create(userProfileData);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        verify(userProfileCreatorImpl).create(any(CreateUserProfileData.class));

    }

    @Test
    public void should_call_retriever_retrieve_method_successfully() {
        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();
        GetUserProfileResponse expected = new GetUserProfileResponse(userProfile);

        when(userProfileRetrieverImpl.retrieve(identifier, false)).thenReturn(userProfile);

        GetUserProfileResponse resource = userProfileService.retrieve(identifier);

        assertThat(resource).isEqualToComparingFieldByField(expected);
        //verify(userProfileRetrieverImpl).retrieve(any(UserProfileIdentifier.class),false);

    }

}
