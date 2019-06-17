package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import lombok.Getter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Getter
public class CreateUserProfileResponse {

    private UUID idamId;
    private Integer idamRegistrationResponse;

    public CreateUserProfileResponse(UserProfile userProfile) {

        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getId();
        this.idamRegistrationResponse = userProfile.getIdamRegistrationResponse();
    }
}

