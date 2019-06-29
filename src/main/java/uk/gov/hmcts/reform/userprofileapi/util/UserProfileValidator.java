package uk.gov.hmcts.reform.userprofileapi.util;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UpdateUserProfileData;

import static java.util.Objects.requireNonNull;

public interface UserProfileValidator {

    String STATUS = "STATUS";
    String LANGUAGEPREFERENCE = "LANGUAGEPREFERENCE";
    String USERTYPE = "USERTYPE";
    String USERCATEGORY = "USERCATEGORY";

    static void isUserIdValid(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new ResourceNotFoundException("userId is null or blank.Should have UUID format");
        }

        try {
            java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Malformed userId.Should have UUID format");
        }
    }

    static void validateUpdateUserProfile (UpdateUserProfileData updateUserProfileData, String userId) {
        isUserIdValid(userId);
        requireNonNull(updateUserProfileData, "updateUserProfileData cannot be null");
        validateEnumField(STATUS, updateUserProfileData.getIdamStatus());
    }

    static boolean isSameAsExistingUserProfile(UpdateUserProfileData updateUserProfileData, UserProfile userProfile) {

        if (!userProfile.getEmail().equals(updateUserProfileData.getEmail().trim()))  {
            return false;
        } else if (!userProfile.getFirstName().equals(updateUserProfileData.getFirstName().trim())) {
            return false;
        } else if (!userProfile.getLastName().equals(updateUserProfileData.getLastName().trim())) {
            return false;
        } else if (!userProfile.getStatus().equals(updateUserProfileData.getIdamStatus().trim())) {
            return false;
        } else {
            return true;
        }
    }

    static void validateEnumField (String name, String value) {
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