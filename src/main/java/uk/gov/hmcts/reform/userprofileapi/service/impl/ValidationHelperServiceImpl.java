package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.TOO_MANY_REQUESTS;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.BADREQUEST;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.ERRORPERSISTINGEXCEPTION;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.REQUIREDFIELDMISSINGEXCEPTION;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.RESOURCENOTFOUNDEXCEPTION;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.TOOMANYREQUESTS;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource.API;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource.SYNC;
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
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;

@Service
@Slf4j
public class ValidationHelperServiceImpl implements ValidationHelperService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ExceptionServiceImpl exceptionService;

    @Value("${resendInterval}")
    private String resendInterval;

    @Override
    public boolean validateUserId(String userId) {
        if (!isUserIdValid(userId, false)) {
            auditService.persistAudit(NOT_FOUND, SYNC);
            final String exceptionMsg = String.format("%s - userId provided is malformed: %s",
                    RESOURCENOTFOUNDEXCEPTION, userId);
            exceptionService.throwCustomRuntimeException(RESOURCENOTFOUNDEXCEPTION, exceptionMsg);
        }
        return true;
    }

    public void validateUserIsPresent(Optional<UserProfile> userProfile) {
        if (!userProfile.isPresent()) {
            auditService.persistAudit(NOT_FOUND, SYNC);
            final String exceptionMsg = String.format("%s - could not find user profile", RESOURCENOTFOUNDEXCEPTION);
            exceptionService.throwCustomRuntimeException(RESOURCENOTFOUNDEXCEPTION, exceptionMsg);
        }
    }

    public boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId,
                                                         ResponseSource source) {
        if (!validateUserProfileStatus(updateUserProfileData)) {
            auditService.persistAudit(BAD_REQUEST, source);
            final String exceptionMsg = String.format("RequiredFieldMissingException - Update user profile request has"
                    .concat(" invalid status %s for userId: %s"), updateUserProfileData.getIdamStatus(), userId);
            exceptionService.throwCustomRuntimeException(REQUIREDFIELDMISSINGEXCEPTION, exceptionMsg);
        }
        return true;
    }

    @Override
    public boolean validateUserStatusBeforeUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile,
                                                  ResponseSource source) {
        if (IdamStatus.PENDING == userProfile.getStatus() || IdamStatus.PENDING.name()
                .equalsIgnoreCase(updateUserProfileData.getIdamStatus())) {
            auditService.persistAudit(BAD_REQUEST, source);
            final String exceptionMsg = String.format("User is PENDING or input status is PENDING and only be changed"
                    .concat(" to ACTIVE or SUSPENDED for userId: %s"), userProfile.getIdamId());
            exceptionService.throwCustomRuntimeException(REQUIREDFIELDMISSINGEXCEPTION, exceptionMsg);
        }
        return true;
    }

    @Override
    public boolean validateUserPersisted(HttpStatus status) {
        if (!status.is2xxSuccessful()) {
            exceptionService.throwCustomRuntimeException(ERRORPERSISTINGEXCEPTION,
                    "Error while persisting user profile");
        }
        return true;
    }

    @Override
    public void validateUserStatus(UserProfile userProfile, IdamStatus expectedStatus) {
        if (expectedStatus != userProfile.getStatus()) {
            auditService.persistAudit(BAD_REQUEST, API);
            final String exceptionMsg = String.format("User is not in %s state", expectedStatus);
            exceptionService.throwCustomRuntimeException(BADREQUEST, exceptionMsg);
        }
    }

    @Override
    public void validateUserLastUpdatedWithinSpecifiedTime(UserProfile userProfile, long expectedMins) {
        if (Duration.between(userProfile.getLastUpdated(), LocalDateTime.now()).toMinutes() < expectedMins) {
            auditService.persistAudit(HttpStatus.TOO_MANY_REQUESTS, API);
            final String exceptionMsg = String.format(TOO_MANY_REQUESTS.getErrorMessage(), resendInterval);
            exceptionService.throwCustomRuntimeException(TOOMANYREQUESTS, exceptionMsg);
        }
    }

    @Override
    public UserProfile validateReInvitedUser(Optional<UserProfile> userProfileOpt) {
        validateUserIsPresent(userProfileOpt);
        UserProfile userProfile = userProfileOpt.orElse(null);
        validateUserStatus(userProfile, IdamStatus.PENDING);
        validateUserLastUpdatedWithinSpecifiedTime(userProfile, Long.valueOf(resendInterval));
        return userProfile;
    }
}
