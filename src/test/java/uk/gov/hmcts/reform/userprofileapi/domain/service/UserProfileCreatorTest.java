package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileCreatorTest {

    @InjectMocks
    private UserProfileCreator userProfileCreator;

    @Mock
    private IdamService idamService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    public void should_create_user_profile_successfully() {

        IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
        CreateUserProfileData createUserProfileData =
            CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        UserProfile userProfile = new UserProfile(createUserProfileData, idamRegistrationInfo);

        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileCreator.create(createUserProfileData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService).registerUser(any(CreateUserProfileData.class));
        inOrder.verify(userProfileRepository).save(any(UserProfile.class));
        verifyNoMoreInteractions(idamService, userProfileRepository);

    }

}
