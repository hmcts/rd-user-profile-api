package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.ErrorPersistingException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource.API;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource.SYNC;


@ExtendWith(MockitoExtension.class)
class ValidationHelperServiceTest {

    @Mock
    private AuditServiceImpl auditServiceMock;

    private final ExceptionServiceImpl exceptionServiceMock = mock(ExceptionServiceImpl.class,
            Mockito.CALLS_REAL_METHODS);

    private final UserProfileCreationData userProfileCreationData
            = CreateUserProfileTestDataBuilder.buildCreateUserProfileData();
    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(ACCEPTED).build());
    private final UserProfile userProfile = new UserProfile(userProfileCreationData,
            idamRegistrationInfo.getIdamRegistrationResponse());
    private final UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("test@test.com",
            "firstName", "lastName", "ACTIVE", new HashSet<>(),
            new HashSet<>());

    @Spy
    @InjectMocks
    private final ValidationHelperService sut = new ValidationHelperServiceImpl();

    @Test
    void test_ValidateUserIdHappyPath() {
        boolean actual = sut.validateUserId("f56e5539-a8f7-4ae6-b378-cc1015b72dcc");

        assertThat(actual).isTrue();
    }

    @Test
    void test_ValidateUserIdException() {
        assertThrows(ResourceNotFoundException.class, () -> sut.validateUserId(""));
    }

    @Test
    void test_ValidateUserIdPersistAuditOnException() {
        final Throwable raisedException = catchThrowable(() -> sut.validateUserId(""));
        assertThat(raisedException).isInstanceOf(ResourceNotFoundException.class);

        verify(auditServiceMock, times(1)).persistAudit(HttpStatus.NOT_FOUND, SYNC);
    }

    @Test
    void test_ValidateUserIsPresentWithExceptionHappyPath() {
        sut.validateUserIsPresent(Optional.of(userProfile));
    }

    @Test
    void test_ValidateUserIsPresentWithException() {
        Optional<UserProfile> empty = Optional.empty();
        assertThrows(ResourceNotFoundException.class, () -> sut.validateUserIsPresent(empty));

    }

    @Test
    void test_ValidateUserIsPresentWithExceptionPersistAuditOnException() {
        doNothing().when(exceptionServiceMock).throwCustomRuntimeException(any(ExceptionType.class), any(String.class));

        sut.validateUserIsPresent(Optional.empty());

        verify(auditServiceMock, times(1)).persistAudit(HttpStatus.NOT_FOUND, SYNC);
    }

    @Test
    void test_ValidateUpdateUserProfileRequestValidHappyPath() {
        updateUserProfileData.setIdamStatus("SUSPENDED");

        boolean actual = sut.validateUpdateUserProfileRequestValid(updateUserProfileData,
                "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", API);

        assertThat(actual).isTrue();
    }

    @Test
    void test_ValidateUpdateUserProfileRequestValidException() {
        updateUserProfileData.setIdamStatus(null);

        assertThrows(RequiredFieldMissingException.class, () ->
                sut.validateUpdateUserProfileRequestValid(updateUserProfileData,
                        "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", API));

    }

    @Test
    void test_ValidateUpdateUserProfileRequestValid_PersistAuditOnException() {
        updateUserProfileData.setIdamStatus(null);

        final Throwable raisedException = catchThrowable(() ->
                sut.validateUpdateUserProfileRequestValid(updateUserProfileData,
                        "f56e5539-a8f7-4ae6-b378-cc1015b72dcc", API));
        assertThat(raisedException).isInstanceOf(RequiredFieldMissingException.class);

        verify(auditServiceMock, times(1)).persistAudit(HttpStatus.BAD_REQUEST, API);
    }

    @Test
    void test_validateUserStatusBeforeUpdate_scenario1() {
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData,
                userProfile, API));

        assertThat(raisedException.getMessage()).contains("User is PENDING or input status is PENDING and only be "
                + "changed to ACTIVE or SUSPENDED for userId: null");
        assertThat(raisedException).isInstanceOf(RequiredFieldMissingException.class);

        verify(auditServiceMock, times(1)).persistAudit(HttpStatus.BAD_REQUEST, API);
    }

    @Test
    void test_validateUserStatusBeforeUpdate_scenario2() {
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData,
                userProfile, API));

        assertThat(raisedException.getMessage()).contains("User is PENDING or input status is PENDING and only be "
                .concat("changed to ACTIVE or SUSPENDED for userId: null"));
        assertThat(raisedException).isInstanceOf(RequiredFieldMissingException.class);

        verify(auditServiceMock, times(1)).persistAudit(HttpStatus.BAD_REQUEST, API);
    }

    @Test
    void test_validateUserStatusBeforeUpdate_scenario3() {
        userProfile.setStatus(IdamStatus.ACTIVE);
        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API)).isTrue();
    }

    @Test
    void test_validateUserStatusBeforeUpdate_should_throw_exception_when_user_is_pending() {
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData,
                userProfile, API));

        assertThat(raisedException.getMessage()).contains("User is PENDING or input status is PENDING and only be "
                .concat("changed to ACTIVE or SUSPENDED for userId: null"));
        assertThat(raisedException).isInstanceOf(RequiredFieldMissingException.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(1))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateUserStatusBeforeUpdate_should_throw_exception_when_user_is_pending_in_request() {
        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData,
                userProfile, API));

        assertThat(raisedException.getMessage()).contains("User is PENDING or input status is PENDING and only be "
                .concat("changed to ACTIVE or SUSPENDED for userId: null"));
        assertThat(raisedException).isInstanceOf(RequiredFieldMissingException.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(1))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateUserPersistedWithException_scenario1() {
        assertThat(sut.validateUserPersisted(HttpStatus.OK)).isTrue();
    }

    @Test
    void test_validateUserPersistedWithException_scenario2() {
        assertThrows(ErrorPersistingException.class, () -> sut.validateUserPersisted(HttpStatus.BAD_REQUEST));
    }

    @Test
    void test_validateUserPersistedWithException_withInvalidHttpStatusCode() {
        assertThrows(ErrorPersistingException.class, () -> sut.validateUserPersisted(HttpStatus.I_AM_A_TEAPOT));
    }


    @Test
    void test_validateUserStatusBeforeUpdate_PendingUserStatus() {
        userProfile.setStatus(IdamStatus.PENDING);

        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatusBeforeUpdate(updateUserProfileData,
                userProfile, API));
        assertThat(raisedException).isInstanceOf(RequiredFieldMissingException.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
    }

    @Test
    void test_validateUserStatusBeforeUpdate_ActiveUserStatus() {
        userProfile.setStatus(IdamStatus.ACTIVE);
        updateUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());

        assertThat(sut.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, API)).isTrue();
    }

    @Test
    void test_validateUserStatusWithException_should_return_true() {
        sut.validateUserStatus(userProfile, IdamStatus.PENDING);
        verify(auditServiceMock, times(0)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(0))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateUserStatusWithException_should_throw_exception() {
        userProfile.setStatus(IdamStatus.ACTIVE);

        final Throwable raisedException = catchThrowable(() -> sut.validateUserStatus(userProfile, IdamStatus.PENDING));
        assertThat(raisedException).isInstanceOf(InvalidRequest.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(1))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateUserLastUpdatedWithinSpecifiedTimeWithException_should_return_true() {

        userProfile.setLastUpdated(LocalDateTime.now().minusMinutes(120L));
        sut.validateUserLastUpdatedWithinSpecifiedTime(userProfile, 60L);
        verify(auditServiceMock, times(0)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(0))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateUserLastUpddWithinSpecifiedTimeWithException_sldRtnTrue_WidExpectedHrsEequalToLocalTime() {

        userProfile.setLastUpdated(LocalDateTime.now().minusMinutes(60L));
        sut.validateUserLastUpdatedWithinSpecifiedTime(userProfile, 60L);
        verify(auditServiceMock, times(0)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(0))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateUserLastUpdatedWithinSpecifiedTimeWithException_should_throw_exception() {
        userProfile.setLastUpdated(LocalDateTime.now());

        final Throwable raisedException = catchThrowable(() ->
                sut.validateUserLastUpdatedWithinSpecifiedTime(userProfile, 60L));
        assertThat(raisedException).isInstanceOf(HttpClientErrorException.class);

        verify(auditServiceMock, times(1)).persistAudit(any(HttpStatus.class),
                any(ResponseSource.class));
        verify(exceptionServiceMock, times(1))
                .throwCustomRuntimeException(any(ExceptionType.class), any(String.class));
    }

    @Test
    void test_validateReInvitedUser_should_return_userProfile() {
        ReflectionTestUtils.setField(sut, "resendInterval", "60");
        userProfile.setLastUpdated(LocalDateTime.now().minusMinutes(120L));
        Optional<UserProfile> userProfileOptional = Optional.of(userProfile);
        UserProfile userProfileResponse = sut.validateReInvitedUser(userProfileOptional, null);
        assertThat(userProfileResponse).isNotNull();
        verify(sut, times(1)).validateUserIsPresent(userProfileOptional);
        verify(sut, times(1)).validateUserStatus(userProfile, IdamStatus.PENDING);
        verify(sut, times(1)).validateUserLastUpdatedWithinSpecifiedTime(any(UserProfile.class),
                anyLong());
    }

    @Test
    void test_validateReInvitedUserFromSrd_should_return_userProfile() {
        ReflectionTestUtils.setField(sut, "resendInterval", "60");
        userProfile.setLastUpdated(LocalDateTime.now().minusMinutes(120L));
        Optional<UserProfile> userProfileOptional = Optional.of(userProfile);
        UserProfile userProfileResponse = sut.validateReInvitedUser(userProfileOptional, "SRD");
        assertThat(userProfileResponse).isNotNull();
        verify(sut, times(1)).validateUserIsPresent(userProfileOptional);
        verify(sut, times(1)).validateUserStatus(userProfile, IdamStatus.PENDING);
        verify(sut, times(0)).validateUserLastUpdatedWithinSpecifiedTime(any(UserProfile.class),
                anyLong());
    }

}
