package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static java.util.Objects.requireNonNull;

import java.util.UUID;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import org.hibernate.annotations.GenericGenerator;

@Entity
public class UserProfile {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private UUID id;
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;

    public UserProfile() {
        //noop
    }

    public UserProfile(String idamId, String email, String firstName, String lastName) {
        requireNonNull(idamId, "idamId must not be null");
        requireNonNull(email, "email must not be null");
        requireNonNull(firstName, "firstname must not be null");
        requireNonNull(lastName, "lastname must not be null");

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
