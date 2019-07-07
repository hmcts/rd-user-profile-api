package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.impl.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileCreatorImpl;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileCreatorImplTest {

    @InjectMocks
    private UserProfileCreatorImpl userProfileCreatorImpl;

    @Mock
    private IdamService idamService;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    @Ignore
    public void should_create_user_profile_successfully() {

        IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
        CreateUserProfileData createUserProfileData =
            CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
        UserProfile userProfile = new UserProfile(createUserProfileData, idamRegistrationInfo);

        when(idamService.registerUser(any())).thenReturn(idamRegistrationInfo);
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileCreatorImpl.create(createUserProfileData);

        assertThat(response).isEqualToComparingFieldByField(userProfile);

        InOrder inOrder = inOrder(idamService, userProfileRepository);
        inOrder.verify(idamService).registerUser(any(CreateUserProfileData.class));
        inOrder.verify(userProfileRepository).save(any(UserProfile.class));
        verifyNoMoreInteractions(idamService, userProfileRepository);

    }

}
