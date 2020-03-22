package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.TOO_MANY_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.ERRORPERSISTINGEXCEPTION;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateUserProfileStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;

@Service
@Slf4j
public class ValidationHelperServiceImpl implements ValidationHelperService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ExceptionService exceptionService;

    @Value("${resendInterval:60}")
    private String resendInterval;

    @Override
    public boolean validateUserIdWithException(String userId) {
        if (!isUserIdValid(userId, false)) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            final String exceptionMsg = String.format("%s - userId provided is malformed: %s", ExceptionType.RESOURCENOTFOUNDEXCEPTION, userId);
            exceptionService.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, exceptionMsg);
        }
        return true;
    }

    public boolean validateUserIsPresentWithException(Optional<UserProfile> userProfile) {
        if (!userProfile.isPresent()) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            final String exceptionMsg = String.format("%s - could not find user profile", ExceptionType.RESOURCENOTFOUNDEXCEPTION);
            exceptionService.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, exceptionMsg);
        }
        return true;
    }

    public boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId, ResponseSource source) {
        if (!validateUserProfileStatus(updateUserProfileData)) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, source);
            final String exceptionMsg = String.format("RequiredFieldMissingException - Update user profile request has invalid status %s for userId: %s", updateUserProfileData.getIdamStatus(), userId);
            exceptionService.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION, exceptionMsg);
        }
        return true;
    }

    @Override
    public boolean validateUserStatusBeforeUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile, ResponseSource source) {
        if (IdamStatus.PENDING == userProfile.getStatus() || IdamStatus.PENDING.name().equalsIgnoreCase(updateUserProfileData.getIdamStatus())) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, source);
            final String exceptionMsg = String.format("User is PENDING or input status is PENDING and only be changed to ACTIVE or SUSPENDED for userId: %s", userProfile.getIdamId());
            exceptionService.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION, exceptionMsg);
        }
        return true;
    }

    @Override
    public boolean validateUserPersistedWithException(HttpStatus status) {
        if (!status.is2xxSuccessful()) {
            exceptionService.throwCustomRuntimeException(ERRORPERSISTINGEXCEPTION, "Error while persisting user profile");
        }
        return true;
    }

    @Override
    public boolean validateUserStatusWithException(UserProfile userProfile, IdamStatus expectedStatus) {
        if (expectedStatus != userProfile.getStatus()) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, ResponseSource.API);
            final String exceptionMsg = String.format("User is not in %s state", expectedStatus);
            exceptionService.throwCustomRuntimeException(ExceptionType.BADREQUEST, exceptionMsg);
        }
        return true;
    }

    @Override
    public boolean validateUserLastUpdatedWithinSpecifiedTimeWithException(UserProfile userProfile, long expectedMins) {
        LocalDateTime latUpdated = userProfile.getLastUpdated();
        if (Duration.between(latUpdated, LocalDateTime.now()).toMinutes() < expectedMins) {
            auditService.persistAudit(HttpStatus.TOO_MANY_REQUESTS, ResponseSource.API);
            final String exceptionMsg = String.format(TOO_MANY_REQUEST.getErrorMessage());
            exceptionService.throwCustomRuntimeException(ExceptionType.TOOMANYREQUEST, exceptionMsg);
        }
        return true;
    }

    public boolean validateReInvitedUser(Optional<UserProfile> userProfileOpt) {
        validateUserIsPresentWithException(userProfileOpt);
        UserProfile userProfile = userProfileOpt.orElse(null);
        validateUserStatusWithException(userProfile, IdamStatus.PENDING);
        validateUserLastUpdatedWithinSpecifiedTimeWithException(userProfile, Long.valueOf(resendInterval));
        return true;
    }
}
