package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
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
        String dummyEmail = "email@gmail.com";
        String dummyFirstName = "April";
        String dummyLastName = "O'Neil";

        when(updateUserProfileDataMock.getEmail()).thenReturn(dummyEmail);
        when(updateUserProfileDataMock.getFirstName()).thenReturn(dummyFirstName);
        when(updateUserProfileDataMock.getLastName()).thenReturn(dummyLastName);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn("SUSPENDED");

        boolean actual = sut.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        verify(updateUserProfileDataMock, times(3)).getEmail();
        verify(updateUserProfileDataMock, times(1)).getFirstName();
        verify(updateUserProfileDataMock, times(1)).getLastName();
        verify(updateUserProfileDataMock, times(2)).getIdamStatus();

        assertThat(actual).isTrue();
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testValidateUpdateUserProfileRequestValidException() {
        when(updateUserProfileDataMock.getEmail()).thenReturn(null);

        doThrow(RequiredFieldMissingException.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION), any(String.class));

        sut.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc");
    }

    @Test
    public void testValidateUpdateUserProfileRequestValidPersistAuditOnException() {
        when(updateUserProfileDataMock.getEmail()).thenReturn(null);

        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUpdateUserProfileRequestValid(updateUserProfileDataMock, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        verify(updateUserProfileDataMock, times(1)).getEmail();
        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(ResponseSource.SYNC));
    }

}
