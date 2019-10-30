package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;


@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private final String userId = "e65e5439-a8f7-4ae6-b378-cc1015b72dbb";
    private final String dummyEmail = "april.o.neil@noreply.com";
    private final String dummyFirstName = "April";
    private final String dummyLastName = "O'Neil";

    private final String userIdNotFound = "f56e5539-a8f7-4ae6-b378-cc1015b72dcc";

    private final String userIdInvalidEmail = "g45e5528-a8f7-4ae6-b378-cc1015b72ddd";
    private final String invalidEmail = "fakeemail.com";

    private final IdamStatus dummyIdamStatus = IdamStatus.SUSPENDED;

    @Mock
    private AuditService auditServiceMock;

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @Mock
    private UpdateUserProfileData updateUserProfileDataMock;

    @Mock
    private ValidationHelperService validationHelperServiceMock;

    @Mock
    private UserProfile userProfileMock;

    @InjectMocks
    private ValidationService sut = new ValidationServiceImpl();

    @Before
    public void setUp() {

        when(userProfileMock.getStatus()).thenReturn(dummyIdamStatus);

        when(userProfileRepositoryMock.findByIdamId(eq(userId))).thenReturn(Optional.of(userProfileMock));

        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE.name());
    }

    @Test
    public void testValidateUpdateWithoutId() {

        when(validationHelperServiceMock.validateUserIdWithException(eq(userId))).thenReturn(true);

        when(validationHelperServiceMock.validateUpdateUserProfileRequestValid(updateUserProfileDataMock,userId)).thenReturn(true);

        UserProfile actual = sut.validateUpdate(updateUserProfileDataMock, userId);

        assertThat(actual.getStatus()).isEqualTo(dummyIdamStatus);
    }

    /*4
    @Override
45
    public boolean isValidForUserDetailUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile) {
46	2
        return userProfile.getStatus().equals(IdamStatus.SUSPENDED) && !userProfile.getStatus().name().equalsIgnoreCase(updateUserProfileData.getIdamStatus());
48
    }*/
    @Test
    public void testIsValidForUserDetailUpdateHappyPath() {
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileDataMock, userProfileMock)).isTrue();
        verify(userProfileMock, times(2)).getStatus();
        verify(updateUserProfileDataMock, times(1)).getIdamStatus();
    }

    @Test
    public void testIsValidForUserDetailUpdateSadPath() {
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.SUSPENDED.name());
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileDataMock, userProfileMock)).isFalse();
        verify(userProfileMock, times(2)).getStatus();
        verify(updateUserProfileDataMock, times(1)).getIdamStatus();
    }


}