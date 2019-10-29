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

// TODO remove this and put in a validaiton service
public interface UserProfileValidator {

    static boolean isUserIdValid(String userId, boolean hasExceptionThrown) {
        if (StringUtils.isBlank(userId)) {
            if (hasExceptionThrown) {
                throw new ResourceNotFoundException("userId is null or blank.");
            }
            return false;
        }
        return true;
    }

    static boolean isUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData) {
        if (validateUpdateUserProfileRequestFields(updateUserProfileData)) {
            return validateUserProfileRequestWithException(updateUserProfileData);
        }
        return false;
    }

    static boolean validateUserProfileRequestWithException(UpdateUserProfileData updateUserProfileData) {
        try {
            validateEnumField(STATUS.name(), updateUserProfileData.getIdamStatus().toUpperCase());
            return true;
        } catch (Exception ex) {
            //TODO log exception?
            return false;
        }
    }


    static boolean validateUpdateUserProfileRequestFields(UpdateUserProfileData updateUserProfileData) {
        return !(null == updateUserProfileData.getEmail()
                || isBlankOrSizeInvalid(updateUserProfileData.getEmail(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getFirstName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getLastName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getIdamStatus(), 255)
                || !updateUserProfileData.getEmail().matches(RegEx.EMAIL.getContent()));
    }

    static boolean isBlankOrSizeInvalid(String fieldValue, int validSize) {

        boolean isInvalid = false;
        if (StringUtils.isBlank(fieldValue) || fieldValue.trim().length() > validSize) {
            isInvalid = true;
        }
        return isInvalid;
    }

    static void validateCreateUserProfileRequest(UserProfileCreationData request) {
        requireNonNull(request, "createUserProfileData cannot be null");

        validateEnumField(USERTYPE.name(), request.getUserType());
        validateEnumField(USERCATEGORY.name(), request.getUserCategory());
    }

    static void validateEnumField(String name, String value) {
        UserProfileField field = UserProfileField.valueOf(name.toUpperCase());

        if (null != value) {
            try {
                switch (field) {
                    case STATUS:
                        IdamStatus.valueOf(value);
                        break;
                    case LANGUAGEPREFERENCE:
                        LanguagePreference.valueOf(value);
                        break;
                    case USERTYPE:
                        UserType.valueOf(value);
                        break;
                    case USERCATEGORY:
                        UserCategory.valueOf(value);
                        break;
                }
            } catch (IllegalArgumentException ex) {
                throw new RequiredFieldMissingException(name + " has invalid value : " + value);
            }
        }
    }

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

    static void validateUserIds(UserProfileDataRequest userProfileDataRequest) {
        if (userProfileDataRequest.getUserIds().isEmpty()) {
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