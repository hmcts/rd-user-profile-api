package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import java.util.UUID;
import lombok.Getter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Getter
public class UserProfileResource {

    private UUID id;
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;

    public UserProfileResource() {
        //noop for deserialization
    }

    public UserProfileResource(UserProfile userProfile) {
        this.id = userProfile.getId();
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
    }

    public UserProfileResource(UUID uuid,
                               String idamIdentifier,
                               String emailAdd,
                               String fName,
                               String lName) {
        this.id = uuid;
        this.idamId = idamIdentifier;
        this.email = emailAdd;
        this.firstName = fName;
        this.lastName = lName;
    }

}
