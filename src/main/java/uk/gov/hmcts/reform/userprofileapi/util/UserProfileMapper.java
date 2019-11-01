package uk.gov.hmcts.reform.userprofileapi.util;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public interface UserProfileMapper {

    static void mapUpdatableFields(UpdateUserProfileData updateUserProfileData, UserProfile userProfile, boolean isExuiUpdate) {
        if (!updateUserProfileData.isSameAsUserProfile(userProfile)) {
            setEmail(updateUserProfileData, userProfile, isExuiUpdate);
            setFirstName(updateUserProfileData, userProfile);
            setLastName(updateUserProfileData, userProfile);
            setStatus(updateUserProfileData, userProfile);
        }
    }

    static UpdateUserDetails mapIdamUpdateStatusRequest(UpdateUserProfileData updateUserProfileData) {
        UpdateUserDetails data = new UpdateUserDetails(updateUserProfileData.getFirstName(), updateUserProfileData.getLastName(), deriveStatusFlag(updateUserProfileData));
        return data;
    }

    static boolean deriveStatusFlag(UpdateUserProfileData data) {

        Boolean deriveStatusFlag = null;
        if (null !=  data.getIdamStatus()) {
            deriveStatusFlag = IdamStatus.ACTIVE.toString().equalsIgnoreCase(data.getIdamStatus()) ? true : false;
        }
        return deriveStatusFlag;

    }

    static void setEmail(UpdateUserProfileData data, UserProfile userProfile, boolean isExuiUpdate) {
        if(!isExuiUpdate) {
            String email = data.getEmail();
            if (StringUtils.isNotEmpty(email)) {
                userProfile.setEmail(email.trim().toLowerCase());
            }
        }
    }

    static void setFirstName(UpdateUserProfileData data, UserProfile userProfile) {
        String firstName = data.getFirstName();
        if (!StringUtils.isBlank(firstName)) {
            userProfile.setFirstName(firstName.trim());
        }
    }

    static void setLastName(UpdateUserProfileData data, UserProfile userProfile) {
        String lastName = data.getLastName();
        if (StringUtils.isBlank(lastName)) {
            userProfile.setLastName(lastName.trim());
        }
    }

    static void setStatus(UpdateUserProfileData data, UserProfile userProfile) {
        String idamStatus = data.getIdamStatus();
        if (StringUtils.isBlank(idamStatus)) {
            userProfile.setStatus(IdamStatus.valueOf(idamStatus.toUpperCase()));
        }
    }
}
