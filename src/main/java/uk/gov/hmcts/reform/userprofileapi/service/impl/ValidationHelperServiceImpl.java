package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUpdateUserProfileRequestValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ExceptionType;
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

    public boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId) {
        if (!isUpdateUserProfileRequestValid(updateUserProfileData)) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, ResponseSource.SYNC);
            final String exceptionMsg = String.format("%s - Update user profile request is not valid for userId: %s",
                    ExceptionType.REQUIREDFIELDMISSINGEXCEPTION.getContent(), userId);
            exceptionService.throwCustomRuntimeException(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION, exceptionMsg);
        }
        return true;
    }

}
