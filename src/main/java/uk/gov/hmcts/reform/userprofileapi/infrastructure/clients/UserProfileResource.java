package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.UUID;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileResource {

    private UUID id;
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;

    public UserProfileResource() {
    }

    public UserProfileResource(UserProfile userProfile) {
        this.id = userProfile.getId();
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
    }

    public UserProfileResource(UUID id,
                               String idamId,
                               String email,
                               String firstName,
                               String lastName) {
        this.id = id;
        this.idamId = idamId;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UUID getId() {
        return id;
    }

    public String getIdamId() {
        return idamId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

}
