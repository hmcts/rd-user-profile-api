package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

import java.util.Optional;

@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    @Mock
    private AuditService auditServiceMock;

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @InjectMocks
    private ValidationService sut = new ValidationServiceImpl();

    @Test
    public void validateUpdate() {
        UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);
        UserProfile dummyUserProfile = Mockito.mock(UserProfile.class);

        final String userId = "e65e5439-a8f7-4ae6-b378-cc1015b72dbb";
        final String dummyEmail = "april.o.neil@noreply.com";
        final String dummyFirstName = "April";
        final String dummyLastName = "O'Neil";

        when(userProfileRepositoryMock.findByIdamId(any())).thenReturn(Optional.of(dummyUserProfile));
        when(updateUserProfileDataMock.getEmail()).thenReturn(dummyEmail);
        when(updateUserProfileDataMock.getFirstName()).thenReturn(dummyFirstName);
        when(updateUserProfileDataMock.getLastName()).thenReturn(dummyLastName);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE.name());

        sut.validateUpdate(updateUserProfileDataMock, userId);

        verify(userProfileRepositoryMock, times(1)).findByIdamId(userId);
    }
}