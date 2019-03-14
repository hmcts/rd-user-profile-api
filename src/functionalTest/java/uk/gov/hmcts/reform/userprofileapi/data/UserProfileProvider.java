package uk.gov.hmcts.reform.userprofileapi.data;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileProvider {

    public static UserProfile aUserProfile() {
        return new UserProfile();
    }

}
