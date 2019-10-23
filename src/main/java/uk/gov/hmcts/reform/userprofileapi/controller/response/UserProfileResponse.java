package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@Getter
@NoArgsConstructor
public class UserProfileResponse {

    @JsonProperty ("userIdentifier")
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;

    public UserProfileResponse(UserProfile userProfile) {
        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.idamStatus = userProfile.getStatus();
    }

}

