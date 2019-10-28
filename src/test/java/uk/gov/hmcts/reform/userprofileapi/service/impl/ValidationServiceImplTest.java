package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
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

    @Mock
    private AuditService auditServiceMock;

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @Mock
    private UpdateUserProfileData updateUserProfileDataMock;

    @InjectMocks
    private ValidationService sut = new ValidationServiceImpl();

    @Before
    public void setUp() {
        UserProfile dummyUserProfile = Mockito.mock(UserProfile.class);
        UserProfile fakeEmailUserProfile = Mockito.mock(UserProfile.class);

        when(userProfileRepositoryMock.findByIdamId(eq(userId))).thenReturn(Optional.of(dummyUserProfile));
        when(userProfileRepositoryMock.findByIdamId(eq(userIdNotFound))).thenReturn(Optional.empty());
        when(userProfileRepositoryMock.findByIdamId(eq(userIdInvalidEmail))).thenReturn(Optional.of(fakeEmailUserProfile));
        when(updateUserProfileDataMock.getEmail()).thenReturn(dummyEmail);
        when(updateUserProfileDataMock.getFirstName()).thenReturn(dummyFirstName);
        when(updateUserProfileDataMock.getLastName()).thenReturn(dummyLastName);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE.name());
    }

    @Test
    public void testValidateUpdate() {
        UserProfile up = sut.validateUpdate(updateUserProfileDataMock, userId);
        verify(userProfileRepositoryMock, times(1)).findByIdamId(userId);
        assertThat(up).isInstanceOf(UserProfile.class);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testValidateUpdateWithoutId() {
        sut.validateUpdate(updateUserProfileDataMock, "");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testValidateUpdateWithEmptyUserProfile() {
        sut.validateUpdate(updateUserProfileDataMock, userIdNotFound);
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testValidateUpdateWithInvalidEmail() {
        when(updateUserProfileDataMock.getEmail()).thenReturn(invalidEmail);
        sut.validateUpdate(updateUserProfileDataMock, userIdInvalidEmail);
    }


}