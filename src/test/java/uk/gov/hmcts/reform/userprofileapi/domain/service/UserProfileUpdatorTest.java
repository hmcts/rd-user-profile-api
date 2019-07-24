package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileUpdator;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileUpdatorTest {

    @InjectMocks
    private UserProfileUpdator userProfileUpdator;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private AuditRepository auditRepository;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);

    private CreateUserProfileData createUserProfileData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com", "firstName", "lastName", "ACTIVE");

    private UserProfile userProfile = new UserProfile(createUserProfileData, idamRegistrationInfo.getIdamRegistrationResponse());


    @Test
    public void should_update_user_profile_successfully() {

        UUID userId = UUID.randomUUID();

        when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenReturn(userProfile);

        UserProfile response = userProfileUpdator.update(updateUserProfileData, userId.toString());

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("email@net.com");
        assertThat(response.getFirstName()).isEqualTo("firstName");
        assertThat(response.getLastName()).isEqualTo("lastName");
        assertThat(response.getStatus()).isEqualTo(IdamStatus.ACTIVE);

        verify(userProfileRepository,times(1)).save(any(UserProfile.class));
        verify(auditRepository,times(1)).save(any(Audit.class));

    }

    @Test
    public void should_throw_ResourceNotFound_when_userId_not_valid() {

        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,"invalid")).isExactlyInstanceOf(ResourceNotFoundException.class);
        verify(auditRepository,times(1)).save(any(Audit.class));
    }

    @Test
    public void should_throw_IdamServiceException_when_user_user_profile_not_found_in_db() {

        UUID userId = UUID.randomUUID();
        when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(null));
        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,userId.toString())).isExactlyInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    public void should_throw_IdamServiceException_when_request_invalid() {

        UUID userId = UUID.randomUUID();
        updateUserProfileData = new UpdateUserProfileData("", "", "", "ACTIV");
        when(userProfileRepository.findByIdamId(userId)).thenReturn(Optional.ofNullable(userProfile));
        assertThatThrownBy(() -> userProfileUpdator.update(updateUserProfileData,userId.toString())).isExactlyInstanceOf(RequiredFieldMissingException.class);
    }

}
