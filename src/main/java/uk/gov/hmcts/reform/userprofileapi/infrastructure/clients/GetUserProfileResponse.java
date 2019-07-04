package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamStatus;

@Getter
@NoArgsConstructor
public class GetUserProfileResponse {

    private UUID idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;

    public GetUserProfileResponse(UserProfile userProfile) {

        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.idamStatus = userProfile.getStatus();
    }

}

