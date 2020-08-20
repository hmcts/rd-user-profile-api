package uk.gov.hmcts.reform.userprofileapi.util;

import java.time.LocalDateTime;
import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

public interface UserProfileMapper {

    static void mapUpdatableFields(UpdateUserProfileData updateUserProfileData, UserProfile userProfile,
                                   boolean isExuiUpdate) {
        if (!updateUserProfileData.isSameAsUserProfile(userProfile)) {
            setEmail(updateUserProfileData.getEmail(), userProfile, isExuiUpdate);
            setFirstName(updateUserProfileData.getFirstName(), userProfile);
            setLastName(updateUserProfileData.getLastName(), userProfile);
            setStatus(updateUserProfileData.getIdamStatus(), userProfile);
        }
    }

    static void mapUpdatableFieldsForReInvite(UserProfileCreationData userProfileCreationData,
                                              UserProfile userProfile) {
        setFirstName(userProfileCreationData.getFirstName(), userProfile);
        setLastName(userProfileCreationData.getLastName(), userProfile);
        // explicitly setting this because hibernate does not update lastupdated
        // if resend invite request has no change in fields
        userProfile.setLastUpdated(LocalDateTime.now());
    }

    static UpdateUserDetails mapIdamUpdateStatusRequest(UpdateUserProfileData updateUserProfileData) {
        return new UpdateUserDetails(updateUserProfileData.getFirstName(), updateUserProfileData.getLastName(),
                deriveStatusFlag(updateUserProfileData));
    }

    static boolean deriveStatusFlag(UpdateUserProfileData data) {
        if (null !=  data.getIdamStatus()) {
            return IdamStatus.ACTIVE.toString().equalsIgnoreCase(data.getIdamStatus());
        }
        return false;
    }

    static void setEmail(String email, UserProfile userProfile, boolean isExuiUpdate) {
        if (!isExuiUpdate && StringUtils.isNotEmpty(email)) {
            userProfile.setEmail(email.trim().toLowerCase());
        }
    }

    static void setFirstName(String  firstName, UserProfile userProfile) {
        if (!StringUtils.isBlank(firstName)) {
            userProfile.setFirstName(firstName.trim());
        }
    }

    static void setLastName(String  lastName, UserProfile userProfile) {
        if (!StringUtils.isBlank(lastName)) {
            userProfile.setLastName(lastName.trim());
        }
    }

    static void setStatus(String idamStatus, UserProfile userProfile) {
        if (!StringUtils.isBlank(idamStatus)) {
            userProfile.setStatus(IdamStatus.valueOf(idamStatus.toUpperCase()));
        }
    }
}
