package uk.gov.hmcts.reform.userprofileapi.resource;

import static uk.gov.hmcts.reform.userprofileapi.controller.advice.UserProfileConstants.NAME_FORMAT_ERROR_MESSAGE;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.UserProfileConstants.NAME_FORMAT_REGEX;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;
import javax.validation.constraints.Pattern;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@NoArgsConstructor
@Getter
@Setter
public class UpdateUserProfileData implements RequestData {

    private String email;

    @Pattern(regexp = NAME_FORMAT_REGEX, message = NAME_FORMAT_ERROR_MESSAGE)
    private String firstName;

    @Pattern(regexp = NAME_FORMAT_REGEX, message = NAME_FORMAT_ERROR_MESSAGE)
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
        return null != userProfile
                && (null != this.getEmail() && userProfile.getEmail().trim().equals(this.getEmail().trim()))
                && (null != this.getFirstName() && userProfile.getFirstName().trim().equals(this.getFirstName().trim()))
                && (null != this.getLastName() && userProfile.getLastName().trim().equals(this.getLastName().trim()))
                && (null != this.getIdamStatus() && userProfile.getStatus().toString().equals(this.getIdamStatus().trim()));
    }

}