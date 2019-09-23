package uk.gov.hmcts.reform.userprofileapi.util;

import static java.util.Objects.requireNonNull;

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

    String STATUS = "STATUS";
    String LANGUAGEPREFERENCE = "LANGUAGEPREFERENCE";
    String USERTYPE = "USERTYPE";
    String USERCATEGORY = "USERCATEGORY";

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

    static void isResponseEntityValidForEmailAndId(String email, String userId) {
        if (StringUtils.isEmpty(email) || StringUtils.isEmpty(userId)) {
            throw new ResourceNotFoundException("Email or User Id are null/empty. ");
        }
    }

    static boolean isUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData) {

        boolean isValid = true;
        if (!validateUpdateUserProfileRequestFields(updateUserProfileData)) {
            isValid = false;
        } else {
            try {
                validateEnumField(STATUS, updateUserProfileData.getIdamStatus().toUpperCase());
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
        } else if (!updateUserProfileData.getEmail().matches("^.*[@].*[.].*$")) {
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

        validateEnumField(USERTYPE, request.getUserType());
        validateEnumField(USERCATEGORY, request.getUserCategory());
    }

    static void validateEnumField(String name, String value) {
        if (null != value) {
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
        } else if (StringUtils.isBlank(userId) || CollectionUtils.isEmpty(userProfileData.getRolesAdd())) {

            throw new RequiredFieldMissingException("No userId or roles in the request");
        }
    }


}