package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;


@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperServiceTest {

    @Mock
    private AuditServiceImpl auditServiceMock;

    @Mock
    private ExceptionService exceptionServiceMock;

    private UserProfileCreationData userProfileCreationData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo.getIdamRegistrationResponse());

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com", "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(), new HashSet<RoleName>());

    @InjectMocks
    private ValidationHelperService sut = new ValidationHelperServiceImpl();

    @Test
    public void testValidateUserIdHappyPath() {
        boolean actual = sut.validateUserIdWithException("f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        assertThat(actual).isTrue();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testValidateUserIdException() {
        doThrow(ResourceNotFoundException.class).when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIdWithException("");
    }

    @Test
    public void testValidateUserIdPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIdWithException("");

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.NOT_FOUND), eq(ResponseSource.SYNC));
    }

    @Test
    public void testValidateUserIsPresentWithExceptionHappyPath() {
        boolean actual = sut.validateUserIsPresentWithException(Optional.empty(), "f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        assertThat(actual).isTrue();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testValidateUserIsPresentWithException() {
        doThrow(ResourceNotFoundException.class).when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIsPresentWithException(Optional.empty(), "f56e5539-a8f7-4ae6-b378-cc1077b72dcc");
    }

    @Test
    public void testValidateUserIsPresentWithExceptionPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIsPresentWithException(Optional.empty(), "f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.NOT_FOUND), eq(ResponseSource.SYNC));
    }

    @Test
    public void testValidateUpdateUserProfileRequestValidHappyPath() {
        updateUserProfileData.setIdamStatus("SUSPENDED");

        boolean actual = sut.validateUpdateUserProfileRequestValid(updateUserProfileData, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", ResponseSource.API);

        assertThat(actual).isTrue();
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testValidateUpdateUserProfileRequestValidException() {
        doThrow(RequiredFieldMissingException.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION), any(String.class));

        updateUserProfileData.setIdamStatus(null);

        sut.validateUpdateUserProfileRequestValid(updateUserProfileData, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", ResponseSource.API);
    }

    @Test
    public void testValidateUpdateUserProfileRequestValidPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        updateUserProfileData.setIdamStatus(null);

        sut.validateUpdateUserProfileRequestValid(updateUserProfileData, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", ResponseSource.API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario1() {
        sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, ResponseSource.API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario2() {
        sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, ResponseSource.API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario3() {
        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, ResponseSource.API)).isTrue();
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserPersistedWithException_scenario1() {
        assertThat(sut.validateUserPersistedWithException(HttpStatus.OK)).isTrue();
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserPersistedWithException_scenario2() {
        assertThat(sut.validateUserPersistedWithException(HttpStatus.BAD_REQUEST)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testvalidateUserPersistedWithException_withInvalidHttpStatus() {
        assertThat(sut.validateUserPersistedWithException(HttpStatus.valueOf("test"))).isTrue();
    }

    @Test
    public void testvalidateUserPersistedWithException_withInvalidHttpStatusCode() {
        assertThat(sut.validateUserPersistedWithException(HttpStatus.I_AM_A_TEAPOT)).isTrue();
    }


    @Test
    public void testvalidateUserStatusBeforeUpdate_PendingUserStatus() {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.PENDING);

        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfileMock, ResponseSource.API)).isTrue();
    }

    @Test
    public void testvalidateUserStatusBeforeUpdate_ActiveUserStatus() {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);

        UpdateUserProfileData updateUserProfileDataMock = mock(UpdateUserProfileData.class);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.PENDING.name());

        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileDataMock, userProfileMock, ResponseSource.API)).isTrue();
    }

}
