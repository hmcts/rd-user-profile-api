package uk.gov.hmcts.reform.userprofileapi.client;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Getter
@NoArgsConstructor
public class GetUserProfilesResponse {

    @JsonProperty
    private List<GetUserProfileWithRolesResponse> userProfiles;

    public GetUserProfilesResponse(List<UserProfile> userProfile, boolean rolesRequired) {
        this.userProfiles = userProfile.stream()
                .map(professionalUser -> new GetUserProfileWithRolesResponse(professionalUser, rolesRequired))
                .collect(toList());
    }
}
