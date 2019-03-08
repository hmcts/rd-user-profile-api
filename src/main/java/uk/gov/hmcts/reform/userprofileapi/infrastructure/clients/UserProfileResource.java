package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import java.util.UUID;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileResource {

    private UUID id;
    private String idamId;
    private String firstName;
    private String lastName;

    public UserProfileResource() {
    }

    public UserProfileResource(UserProfile userProfile) {
        this.id = userProfile.getId();
        this.idamId = userProfile.getIdamId();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
    }

    public UserProfileResource(UUID id, String idamId, String firstName, String lastName) {
        this.id = id;
        this.idamId = idamId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public String getIdamId() {
        return idamId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
