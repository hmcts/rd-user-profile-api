package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class GetUserProfilesResponse {

    @JsonProperty
    private final List<GetUserProfileWithRolesResponse> userProfiles;

    public GetUserProfilesResponse(List<UserProfile> userProfile) {
        this.userProfiles = userProfile.stream()
                .map(professionalUser -> new GetUserProfileWithRolesResponse(professionalUser))
                .collect(toList());
    }
}
