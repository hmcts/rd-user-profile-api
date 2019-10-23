package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static java.util.Objects.requireNonNull;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileCreationResponse {

    private String idamId;
    private Integer idamRegistrationResponse;

    public UserProfileCreationResponse(UserProfile userProfile) {

        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.idamRegistrationResponse = userProfile.getIdamRegistrationResponse();
    }
}

