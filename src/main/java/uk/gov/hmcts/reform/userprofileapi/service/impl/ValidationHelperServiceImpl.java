package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType.ERRORPERSISTINGEXCEPTION;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.validateUserProfileStatus;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ValidationHelperServiceImpl implements ValidationHelperService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private ExceptionService exceptionService;

    @Override
    public boolean validateUserIdWithException(String userId) {
        if (!isUserIdValid(userId, false)) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            final String exceptionMsg = String.format("%s - userId provided is malformed: %s", ExceptionType.RESOURCENOTFOUNDEXCEPTION, userId);
            exceptionService.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, exceptionMsg);
        }
        return true;
    }

    public boolean validateUserIsPresentWithException(Optional<UserProfile> userProfile, String userId) {
        if (!userProfile.isPresent()) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            final String exceptionMsg = String.format("%s - could not find user profile for userId: %s", ExceptionType.RESOURCENOTFOUNDEXCEPTION, userId);
            exceptionService.throwCustomRuntimeException(ExceptionType.RESOURCENOTFOUNDEXCEPTION, exceptionMsg);
        }
        return true;
    }

    public boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId, ResponseSource source) {
        if (!validateUserProfileStatus(updateUserProfileData)) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, source);
            final String exceptionMsg = String.format("RequiredFieldMissingException - Update user profile request is not valid for userId: %s", userId);
            exceptionService.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION, exceptionMsg);
        }
        return true;
    }

    @Override
    public boolean validateUserStatusBeforeUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile, ResponseSource source) {
        if (IdamStatus.PENDING == userProfile.getStatus() || IdamStatus.PENDING.toString().equalsIgnoreCase(updateUserProfileData.getIdamStatus())) {
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
}
