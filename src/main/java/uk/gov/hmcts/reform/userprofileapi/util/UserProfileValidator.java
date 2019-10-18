package uk.gov.hmcts.reform.userprofileapi.util;

import static java.util.Objects.requireNonNull;
import static uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant.EXUI;
import static uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant.LANGUAGE_PREFERENCE;
import static uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant.STATUS;
import static uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant.USER_CATEGORY;
import static uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant.USER_TYPE;

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

        boolean isValid = true;
        if (updateUserProfileData == null) {
            isValid = false;
        } else {
            try {
                validateEnumField(STATUS, updateUserProfileData.getIdamStatus().toUpperCase());
            } catch (Exception ex) {
                isValid = false;
            }
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

    static boolean isBlankOrSizeInvalid(String fieldValue, int validSize) {

        boolean isInvalid = false;
        if (StringUtils.isBlank(fieldValue) || fieldValue.trim().length() > validSize) {
            isInvalid = true;
        }
        return isInvalid;
    }

    static boolean isSameAsExistingUserProfile(UpdateUserProfileData updateUserProfileData, UserProfile userProfile) {

        boolean isSame = false;
        if (userProfile.getFirstName().equals(updateUserProfileData.getFirstName().trim())
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
                || (!CollectionUtils.isEmpty(userProfileData.getRolesAdd()) && userProfileData.getRolesAdd().size() == 0)
                || (!CollectionUtils.isEmpty(userProfileData.getRolesDelete()) && userProfileData.getRolesDelete().size() == 0)
                || (null != userProfileData.getIdamStatus() && userProfileData.getIdamStatus().trim().length() == 0)) {


            throw new RequiredFieldMissingException("No userId or roles in the request");
        }
    }

    static boolean isUpdateFromExui(String param) {

        return EXUI.equalsIgnoreCase(param) ? true : false;
    }

    static Boolean deriveStatusFlag(UpdateUserProfileData data) {

        Boolean deriveStatusFlag = null;
        if (null !=  data.getIdamStatus()) {
            deriveStatusFlag = IdamStatus.ACTIVE.toString().equalsIgnoreCase(data.getIdamStatus()) ? true : false;
        }
        return deriveStatusFlag;

    }

}