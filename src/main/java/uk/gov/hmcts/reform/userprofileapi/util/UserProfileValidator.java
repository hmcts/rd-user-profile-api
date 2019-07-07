package uk.gov.hmcts.reform.userprofileapi.util;

import static java.util.Objects.requireNonNull;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;

public interface UserProfileValidator {

    String STATUS = "STATUS";
    String LANGUAGEPREFERENCE = "LANGUAGEPREFERENCE";
    String USERTYPE = "USERTYPE";
    String USERCATEGORY = "USERCATEGORY";

    static boolean isUserIdValid(String userId, boolean throwException) {
        if (StringUtils.isBlank(userId)) {
            if (throwException) {
                throw new ResourceNotFoundException("userId is null or blank.Should have UUID format");
            }
            return false;
        }

        try {
            java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            if (throwException) {
                throw new ResourceNotFoundException("Malformed userId.Should have UUID format");
            }
            return false;
        }
        return true;
    }

    static boolean isUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData) {

        boolean isValid = true;

        if (!validateUpdateUserProfileRequestFields(updateUserProfileData)) {
            isValid = false;
        } else {
            try {
                validateEnumField(STATUS, updateUserProfileData.getIdamStatus());
            } catch (Exception ex) {
                isValid = false;
            }
        }
        return isValid;
    }

    static boolean validateUpdateUserProfileRequestFields(UpdateUserProfileData updateUserProfileData) {

        boolean isValid = true;
        if (updateUserProfileData == null) {
            isValid = false;
        } else if (isBlankOrSizeInvalid(updateUserProfileData.getEmail(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getFirstName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getLastName(), 255)
                || isBlankOrSizeInvalid(updateUserProfileData.getIdamStatus(), 255)) {

            isValid = false;
        } else if (!updateUserProfileData.getEmail().matches("\\A(?=[a-zA-Z0-9@.!#$%&'*+/=?^_`{|}~-]{6,254}\\z)(?=[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:(?=[a-zA-Z0-9-]{1,63}\\.)[a-zA-Z0-9](?:[a-z0-9-]*[a-zA-Z0-9])?\\.)+(?=[a-zA-Z0-9-]{1,63}\\z)[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\z")) {
            isValid = false;
        }
        return isValid;
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

        validateEnumField(LANGUAGEPREFERENCE, request.getLanguagePreference());
        validateEnumField(USERTYPE, request.getUserType());
        validateEnumField(USERCATEGORY, request.getUserCategory());
    }

    static void validateEnumField(String name, String value) {
        try {
            if (name.equals(STATUS)) {
                IdamStatus.valueOf(value);
            } else if (name.equals(LANGUAGEPREFERENCE)) {
                LanguagePreference.valueOf(value);
            } else if (name.equals(USERTYPE)) {
                UserType.valueOf(value);
            } else if (name.equals(USERCATEGORY)) {
                UserCategory.valueOf(value);
            }
        } catch (IllegalArgumentException ex) {
            throw new RequiredFieldMissingException(name + " has invalid value : " + value);
        }

    }
}