package uk.gov.hmcts.reform.userprofileapi.util;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

public interface UserProfileMapper {

    /*static void mapUpdatableFields(UpdateUserProfileData updateUserProfileData, UserProfile userProfile) {
        if (!updateUserProfileData.isSameAsUserProfile(userProfile)) {
            setEmail(updateUserProfileData, userProfile);
            setFirstName(updateUserProfileData, userProfile);
            setLastName(updateUserProfileData, userProfile);
            setStatus(updateUserProfileData, userProfile);
        }
    }*/

    static void setEmail(UpdateUserProfileData data, UserProfile userProfile) {
        String email = data.getEmail().trim().toLowerCase();
        if (StringUtils.isNotEmpty(email)) {
            userProfile.setEmail(email);
        }
    }

    static void setFirstName(UpdateUserProfileData data, UserProfile userProfile) {
        String firstName = data.getFirstName().trim();
        if (StringUtils.isNotEmpty(firstName)) {
            userProfile.setFirstName(firstName);
        }
    }

    static void setLastName(UpdateUserProfileData data, UserProfile userProfile) {
        String lastName = data.getLastName().trim();
        if (StringUtils.isNotEmpty(lastName)) {
            userProfile.setLastName(lastName);
        }
    }

    static void setStatus(UpdateUserProfileData data, UserProfile userProfile) {
        String idamStatus = data.getIdamStatus();
        if (StringUtils.isNotEmpty(idamStatus)) {
            userProfile.setStatus(IdamStatus.valueOf(idamStatus.toUpperCase()));
        }
    }
}
