package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.TOOMANYREQUESTS;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource.API;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource.SYNC;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
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


    private UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();
    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo.getIdamRegistrationResponse());

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com", "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(), new HashSet<RoleName>());

    @InjectMocks
    private ValidationHelperService sut = new ValidationHelperServiceImpl();

    @Test
    public void testValidateUserIdHappyPath() {
        boolean actual = sut.validateUserId("f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        assertThat(actual).isTrue();
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testValidateUserIdException() {
        doThrow(ResourceNotFoundException.class).when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserId("");
    }

    @Test
    public void testValidateUserIdPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserId("");

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.NOT_FOUND), eq(SYNC));
    }

    @Test(expected = Test.None.class)
    public void testValidateUserIsPresentWithExceptionHappyPath() {
        sut.validateUserIsPresent(Optional.of(userProfile));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testValidateUserIsPresentWithException() {
        doThrow(ResourceNotFoundException.class).when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIsPresent(Optional.empty());
    }

    @Test
    public void testValidateUserIsPresentWithExceptionPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIsPresent(Optional.empty());

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.NOT_FOUND), eq(SYNC));
    }

    @Test
    public void testValidateUpdateUserProfileRequestValidHappyPath() {
        updateUserProfileData.setIdamStatus("SUSPENDED");

        boolean actual = sut.validateUpdateUserProfileRequestValid(updateUserProfileData, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", API);

        assertThat(actual).isTrue();
    }

    @Test(expected = RequiredFieldMissingException.class)
    public void testValidateUpdateUserProfileRequestValidException() {
        doThrow(RequiredFieldMissingException.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION), any(String.class));

        updateUserProfileData.setIdamStatus(null);

        sut.validateUpdateUserProfileRequestValid(updateUserProfileData, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", API);
    }

    @Test
    public void testValidateUpdateUserProfileRequestValidPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        updateUserProfileData.setIdamStatus(null);

        sut.validateUpdateUserProfileRequestValid(updateUserProfileData, "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario1() {
        sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario2() {
        sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API);

        verify(auditServiceMock, times(1)).persistAudit(eq(HttpStatus.BAD_REQUEST), eq(API));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserStatusBeforeUpdate_scenario3() {
        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API)).isTrue();
    }

    @Test
    public void test_validateUserStatusBeforeUpdate_should_throw_exception_when_user_is_pending() {

        doThrow(InvalidRequest.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION), any(String.class));
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API));
        assertThat(raisedException).isInstanceOf(InvalidRequest.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
        verify(exceptionServiceMock, times(1)).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    public void test_validateUserStatusBeforeUpdate_should_throw_exception_when_user_is_pending_in_request() {

        doThrow(InvalidRequest.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION), any(String.class));
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API));
        assertThat(raisedException).isInstanceOf(InvalidRequest.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
        verify(exceptionServiceMock, times(1)).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserPersistedWithException_scenario1() {
        assertThat(sut.validateUserPersisted(HttpStatus.OK)).isTrue();
    }

    @Test(expected = Test.None.class)
    public void testvalidateUserPersistedWithException_scenario2() {
        assertThat(sut.validateUserPersisted(HttpStatus.BAD_REQUEST)).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testvalidateUserPersistedWithException_withInvalidHttpStatus() {
        assertThat(sut.validateUserPersisted(HttpStatus.valueOf("test"))).isTrue();
    }

    @Test
    public void testvalidateUserPersistedWithException_withInvalidHttpStatusCode() {
        assertThat(sut.validateUserPersisted(HttpStatus.I_AM_A_TEAPOT)).isTrue();
    }


    @Test
    public void testvalidateUserStatusBeforeUpdate_PendingUserStatus() {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.PENDING);

        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfileMock, API)).isTrue();

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
    }

    @Test
    public void testvalidateUserStatusBeforeUpdate_ActiveUserStatus() {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);

        UpdateUserProfileData updateUserProfileDataMock = mock(UpdateUserProfileData.class);
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.PENDING.name());

        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileDataMock, userProfileMock, API)).isTrue();
    }

    @Test(expected = Test.None.class)
    public void test_validateUserStatusWithException_should_return_true() {
        sut.validateUserStatus(userProfile, IdamStatus.PENDING);
        verify(auditServiceMock, times(0)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
        verify(exceptionServiceMock, times(0)).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    public void test_validateUserStatusWithException_should_throw_exception() {

        userProfile.setStatus(IdamStatus.ACTIVE);
        doThrow(InvalidRequest.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(ExceptionType.BADREQUEST), any(String.class));
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatus(userProfile, IdamStatus.PENDING));
        assertThat(raisedException).isInstanceOf(InvalidRequest.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
        verify(exceptionServiceMock, times(1)).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test(expected = Test.None.class)
    public void test_validateUserLastUpdatedWithinSpecifiedTimeWithException_should_return_true() {

        userProfile.setLastUpdated(LocalDateTime.now().minusMinutes(120L));
        sut.validateUserLastUpdatedWithinSpecifiedTime(userProfile, 60L);
        verify(auditServiceMock, times(0)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
        verify(exceptionServiceMock, times(0)).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    public void test_validateUserLastUpdatedWithinSpecifiedTimeWithException_should_throw_exception() {

        userProfile.setLastUpdated(LocalDateTime.now());
        doThrow(HttpClientErrorException.class).when(exceptionServiceMock).throwCustomRuntimeException(eq(TOOMANYREQUESTS), any(String.class));
        final Throwable raisedException = catchThrowable(() -> sut.validateUserLastUpdatedWithinSpecifiedTime(userProfile, 60L));
        assertThat(raisedException).isInstanceOf(HttpClientErrorException.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class), any(ResponseSource.class));
        verify(exceptionServiceMock, times(1)).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    public void test_validateReInvitedUser_should_return_userProfile() {

        ReflectionTestUtils.setField(sut, "resendInterval", "60");
        userProfile.setLastUpdated(LocalDateTime.now().minusMinutes(120L));
        Optional<UserProfile> userProfileOptional = Optional.of(userProfile);
        UserProfile userProfileResponse  = sut.validateReInvitedUser(userProfileOptional);
        assertThat(userProfileResponse).isNotNull();
    }
}
