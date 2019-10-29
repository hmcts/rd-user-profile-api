package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ExceptionService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;

import java.util.Optional;

import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUpdateUserProfileRequestValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;

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
            final String exceptionMsg = String.format("ResourceNotFoundException", "userId provided is malformed: ", userId);
            exceptionService.throwCustomRuntimeException("ResourceNotFoundException", exceptionMsg);
        }
        return true;
    }

    public boolean validateUserIsPresentWithException(Optional<UserProfile> userProfile, String userId){
        if (!userProfile.isPresent()) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            final String exceptionMsg = String.format("ResourceNotFoundException", "could not find user profile for userId: ", userId);
            exceptionService.throwCustomRuntimeException("ResourceNotFoundException", exceptionMsg);
        }
        return true;
    }

    public boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId){
        if (!isUpdateUserProfileRequestValid(updateUserProfileData)) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, ResponseSource.SYNC);
            final String exceptionMsg = String.format("RequiredFieldMissingException", "Update user profile request is not valid for userId: ", userId);
            exceptionService.throwCustomRuntimeException("RequiredFieldMissingException", exceptionMsg);
        }
        return true;
    }
}
