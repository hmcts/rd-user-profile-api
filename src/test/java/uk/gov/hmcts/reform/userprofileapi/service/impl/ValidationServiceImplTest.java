package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;


@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private final String userId = UUID.randomUUID().toString();

    private final IdamStatus dummyIdamStatus = IdamStatus.SUSPENDED;

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
    }

    @Test
    public void testValidateUpdateWithoutId() {
        when(validationHelperServiceMock.validateUserIdWithException(eq(userId))).thenReturn(true);
        when(validationHelperServiceMock.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, userId, ResponseSource.API)).thenReturn(true);

        UserProfile actual = sut.validateUpdate(updateUserProfileDataMock, userId, ResponseSource.API);

        assertThat(actual.getStatus()).isEqualTo(dummyIdamStatus);

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
    }

    @Test
    public void testIsValidForUserDetailUpdateHappyPath() {
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileDataMock, userProfileMock, ResponseSource.API)).isFalse();
    }

    @Test
    public void testIsValidForUserDetailUpdateSadPath() {
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileDataMock, userProfileMock, ResponseSource.API)).isFalse();
    }

    @Test
    public void testIsExuiUpdateRequest() {
        assertThat(sut.isExuiUpdateRequest(ResponseSource.EXUI.name())).isTrue();
    }

}