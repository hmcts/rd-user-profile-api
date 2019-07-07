package uk.gov.hmcts.reform.userprofileapi.clients;

import static java.util.stream.Collectors.toList;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Getter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Getter
public class GetUserProfilesResponse {

    @JsonProperty
    private final List<GetUserProfileWithRolesResponse> userProfiles;

    public GetUserProfilesResponse(List<UserProfile> userProfile) {
        this.userProfiles = userProfile.stream()
                .map(professionalUser -> new GetUserProfileWithRolesResponse(professionalUser))
                .collect(toList());
    }
}
