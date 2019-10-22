package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@NoArgsConstructor
@Getter
@Setter
public class UpdateUserProfileData implements RequestData {

    private String email;

    private String firstName;

    private String lastName;

    private String idamStatus;

    private Set<RoleName> rolesAdd;

    private Set<RoleName> rolesDelete;

    @JsonCreator
    public UpdateUserProfileData(@JsonProperty(value = "email") String email,
                                 @JsonProperty(value = "firstName") String firstName,
                                 @JsonProperty(value = "lastName") String lastName,
                                 @JsonProperty(value = "idamStatus") String idamStatus,
                                 @JsonProperty(value = "rolesAdd") Set<RoleName> rolesAdd,
                                 @JsonProperty(value = "rolesDelete") Set<RoleName> rolesDelete) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idamStatus = idamStatus;
        this.rolesAdd = rolesAdd;
        this.rolesDelete = rolesDelete;
    }


    public boolean isSameAsUserProfile(UserProfile userProfile) {
        return userProfile.getEmail().trim().equalsIgnoreCase(this.getEmail().trim())
                && userProfile.getFirstName().trim().equals(this.getFirstName().trim())
                && userProfile.getLastName().trim().equals(this.getLastName().trim())
                && userProfile.getStatus().toString().equals(this.getIdamStatus().trim());
    }

}
