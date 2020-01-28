package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;


@RunWith(MockitoJUnitRunner.class)
public class ValidationHelperServiceTest {

    @Mock
    private UserProfile userProfileMock;

    @Mock
    private UpdateUserProfileData updateUserProfileDataMock;

    @Mock
    private AuditServiceImpl auditServiceMock;

    @Mock
    private ExceptionService exceptionServiceMock;


    @InjectMocks
    private ValidationHelperService sut = new ValidationHelperServiceImpl();

    //SCENARIO ONE-------------------------------------------------------------------------------------------------------------------------
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

    //SCENARIO TWO-------------------------------------------------------------------------------------------------------------------------
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

    //SCENARIO THREE-------------------------------------------------------------------------------------------------------------------------
    @Test
    public void testValidateUpdateUserProfileRequestValidHappyPath() {
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn("SUSPENDED");

        boolean actual = sut.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", ResponseSource.API);

        assertThat(actual).isTrue();

        Mockito.verify(updateUserProfileDataMock, Mockito.times(1)).getIdamStatus();

    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testValidateUpdateUserProfileRequestValidException() {
        doThrow(RequiredFieldMissingException.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION), any(String.class));

        sut.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", ResponseSource.API);
    }

    @Test
    public void testValidateUpdateUserProfileRequestValidPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", ResponseSource.API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario1() {
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.PENDING);

        sut.validateUserStatusBeforeUpdate(updateUserProfileDataMock, userProfileMock, ResponseSource.API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario2() {
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.PENDING.name());

        sut.validateUserStatusBeforeUpdate(updateUserProfileDataMock, userProfileMock, ResponseSource.API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario3() {
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE.name());

        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileDataMock, userProfileMock, ResponseSource.API)).isTrue();
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserPersistedWithException_scenario1() {
        assertThat(sut.validateUserPersistedWithException(HttpStatus.OK)).isTrue();
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserPersistedWithException_scenario2() {
        assertThat(sut.validateUserPersistedWithException(HttpStatus.BAD_REQUEST)).isTrue();
    }

}
