package uk.gov.hmcts.reform.userprofileapi.util;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.UserProfileField.*;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.*;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

//TODO change this to an injectable service with easy to maintain code inside
public interface UserProfileValidator {

    String EMAIL_REGEX = "^.*[@].*[.].*$";

    //TODO refactor and remove this!
    static boolean isUserIdValid(String userId, boolean throwException) {
        boolean valid = true;
        if (StringUtils.isBlank(userId)) {
            valid = false;
            if (throwException) {
                throw new ResourceNotFoundException("userId is null or blank.");
            }
        }
        return valid;
    }

    //TODO refactor and remove this!
    static boolean isUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData) {
        return validateUpdateUserProfileRequestFields(updateUserProfileData)
                && validateUserProfileRequestWithException(updateUserProfileData);
    }

    //TODO refactor and remove this!
    static boolean validateUserProfileRequestWithException(UpdateUserProfileData updateUserProfileData) {
        try {
            validateEnumField(STATUS.name(), updateUserProfileData.getIdamStatus().toUpperCase());
        } catch (Exception ex) {
            //TODO log exception
            return false;
        }
        return true;
    }

    //TODO refactor and remove this!
    static boolean validateUpdateUserProfileRequestFields(UpdateUserProfileData updateUserProfileData) {
        return !(null == updateUserProfileData.getEmail()
                || isBlankOrSizeInvalid(updateUserProfileData.getEmail(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getFirstName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getLastName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getIdamStatus(), 255)
                || !updateUserProfileData.getEmail().matches(EMAIL_REGEX));
    }

    //TODO refactor and remove this!
    static boolean isBlankOrSizeInvalid(String fieldValue, int validSize) {
        return StringUtils.isBlank(fieldValue) || fieldValue.trim().length() > validSize;
    }

    //TODO refactor and remove this!
    static void validateCreateUserProfileRequest(UserProfileCreationData request) {
        requireNonNull(request, "createUserProfileData cannot be null");

        validateEnumField(USERTYPE.name(), request.getUserType());
        validateEnumField(USERCATEGORY.name(), request.getUserCategory());
    }

    //TODO refactor and remove this!
    static void validateEnumField(String name, String value) {
        if (null != value) {
            try {
                if (name.equals(STATUS.name())) {
                    IdamStatus.valueOf(value);
                } else if (name.equals(LANGUAGEPREFERENCE.name())) {
                    LanguagePreference.valueOf(value);
                } else if (name.equals(USERTYPE.name())) {
                    UserType.valueOf(value);
                } else if (name.equals(USERCATEGORY.name())) {
                    UserCategory.valueOf(value);
                }
            } catch (IllegalArgumentException ex) {
                throw new RequiredFieldMissingException(name + " has invalid value : " + value);
            }
        }
    }

    //TODO refactor and remove this
    static boolean validateAndReturnBooleanForParam(String param) {

        boolean isValid;
        if (null == param) {
            throw new RequiredFieldMissingException("param has invalid value : " + param);
        } else if ("true".equalsIgnoreCase(param)) {
            isValid = true;
        } else if ("false".equalsIgnoreCase(param)) {
            isValid = false;
        } else {
            throw new RequiredFieldMissingException("param showDeleted has invalid value : " + param);
        }
        return isValid;
    }

    //TODO refactor and remove this
    static void validateUserIds(UserProfileDataRequest userProfileDataRequest) {
        if (userProfileDataRequest.getUserIds().isEmpty()) {
            throw new RequiredFieldMissingException("no user id in request");
        }
    }

    //TODO refactor and remove this
    static void validateUserProfileDataAndUserId(UpdateUserProfileData userProfileData, String userId) {

        if (null == userProfileData) {
            throw new RequiredFieldMissingException("No Request Body in the request");
        }

        if (StringUtils.isBlank(userId)) {
            throw new RequiredFieldMissingException("No userId in the request");
        }
    }

    //TODO refactor and remove this
    static boolean hasDataAndId(UpdateUserProfileData userProfileData, String userId) {
        return null != userProfileData && StringUtils.isNotBlank(userId);
    }

    //TODO refactor and remove this
    static boolean hasRolesToUpdate(UpdateUserProfileData data) {
        return !(CollectionUtils.isEmpty(data.getRolesAdd())
                && CollectionUtils.isEmpty(data.getRolesDelete()));
    }
}