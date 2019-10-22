package uk.gov.hmcts.reform.userprofileapi.util;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant.*;

import org.apache.commons.lang.StringUtils;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesRequest;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;

public interface UserProfileValidator {

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

    static boolean isUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData) {
        if (validateUpdateUserProfileRequestFields(updateUserProfileData)) {
            return validateUserProfileRequestWithException(updateUserProfileData);
        }
        return false;
    }

    static boolean validateUserProfileRequestWithException(UpdateUserProfileData updateUserProfileData) {
        try {
            validateEnumField(STATUS, updateUserProfileData.getIdamStatus().toUpperCase());
        } catch (Exception ex) {
            //TODO log exception
            return false;
        }
        return true;
    }

    static boolean validateUpdateUserProfileRequestFields(UpdateUserProfileData updateUserProfileData) {
        return !(null == updateUserProfileData.getEmail()
                || isBlankOrSizeInvalid(updateUserProfileData.getEmail(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getFirstName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getLastName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getIdamStatus(), 255)
                || !updateUserProfileData.getEmail().matches(EMAIL_REGEX));
    }

    static boolean isBlankOrSizeInvalid(String fieldValue, int validSize) {

        boolean isInvalid = false;
        if (StringUtils.isBlank(fieldValue) || fieldValue.trim().length() > validSize) {
            isInvalid = true;
        }
        return isInvalid;
    }

    static boolean isSameAsExistingUserProfile(UpdateUserProfileData updateUserProfileData, UserProfile userProfile) {

        boolean isSame = false;
        if (userProfile.getEmail().equals(updateUserProfileData.getEmail().trim())
                && userProfile.getFirstName().equals(updateUserProfileData.getFirstName().trim())
                && userProfile.getLastName().equals(updateUserProfileData.getLastName().trim())
                && userProfile.getStatus().toString().equals(updateUserProfileData.getIdamStatus().trim())) {
            isSame = true;
        }
        return isSame;
    }

    static void validateCreateUserProfileRequest(CreateUserProfileData request) {
        requireNonNull(request, "createUserProfileData cannot be null");

        validateEnumField(USER_TYPE, request.getUserType());
        validateEnumField(USER_CATEGORY, request.getUserCategory());
    }

    static void validateEnumField(String name, String value) {
        if (null != value) {
            try {
                if (name.equals(STATUS)) {
                    IdamStatus.valueOf(value);
                } else if (name.equals(LANGUAGE_PREFERENCE)) {
                    LanguagePreference.valueOf(value);
                } else if (name.equals(USER_TYPE)) {
                    UserType.valueOf(value);
                } else if (name.equals(USER_CATEGORY)) {
                    UserCategory.valueOf(value);
                }
            } catch (IllegalArgumentException ex) {
                throw new RequiredFieldMissingException(name + " has invalid value : " + value);
            }
        }
    }

    static boolean validateAndReturnBooleanForParam(String param) {

        boolean isValid = false;
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

    static void validateUserIds(GetUserProfilesRequest getUserProfilesRequest) {
        if (getUserProfilesRequest.getUserIds().isEmpty()) {
            throw new RequiredFieldMissingException("no user id in request");
        }
    }

    static void validateUserProfileDataAndUserId(UpdateUserProfileData userProfileData, String userId) {

        if (null == userProfileData) {

            throw new RequiredFieldMissingException("No Request Body in the request");
        } else if (StringUtils.isBlank(userId)
                || (!CollectionUtils.isEmpty(userProfileData.getRolesAdd()) && userProfileData.getRolesAdd().isEmpty())
                || (!CollectionUtils.isEmpty(userProfileData.getRolesDelete()) && userProfileData.getRolesDelete().isEmpty())) {

            throw new RequiredFieldMissingException("No userId or roles in the request");
        }
    }
}