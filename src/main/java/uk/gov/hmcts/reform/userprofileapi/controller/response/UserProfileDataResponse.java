package uk.gov.hmcts.reform.userprofileapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import java.util.List;

@Getter
@NoArgsConstructor
public class UserProfileDataResponse {

    @JsonProperty
    private List<UserProfileWithRolesResponse> userProfiles;

    public UserProfileDataResponse(List<UserProfile> userProfile, boolean rolesRequired) {
        this.userProfiles = userProfile.stream()
                .map(professionalUser -> new UserProfileWithRolesResponse(professionalUser, rolesRequired))
                .toList();
    }
}
