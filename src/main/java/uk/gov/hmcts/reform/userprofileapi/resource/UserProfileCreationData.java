package uk.gov.hmcts.reform.userprofileapi.resource;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

@Setter
@Getter
@NoArgsConstructor
public class UserProfileCreationData implements RequestData {

    @Email(regexp = "^.*[@].*[.].*$")
    @NotBlank (message = "email must not be null or blank")
    private String email;

    @NotBlank (message = "firstName must not be null or blank")
    private String firstName;

    @NotBlank (message = "lastName must not be null or blank")
    private String lastName;

    private String languagePreference;

    private String userCategory;

    private String userType;

    private boolean emailCommsConsent;

    private boolean postalCommsConsent;

    @NotEmpty(message = "at least one role is required")
    private List<String> roles;

    @JsonIgnore
    private IdamStatus status;

    private boolean resendInvite;

    @JsonCreator
    public UserProfileCreationData(@JsonProperty(value = "email") String email,
                                   @JsonProperty(value = "firstName") String firstName,
                                   @JsonProperty(value = "lastName") String lastName,
                                   @JsonProperty(value = "languagePreference") String languagePreference,
                                   @JsonProperty(value = "emailCommsConsent") boolean emailCommsConsent,
                                   @JsonProperty(value = "postalCommsConsent") boolean postalCommsConsent,
                                   @JsonProperty(value = "userCategory") String userCategory,
                                   @JsonProperty(value = "userType") String userType,
                                   @JsonProperty(value = "roles") List<String> roles,
                                   @JsonProperty(value = "resendInvite") boolean resendInvite) {

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languagePreference = languagePreference;
        this.emailCommsConsent = emailCommsConsent;
        this.postalCommsConsent = postalCommsConsent;
        this.userCategory = userCategory;
        this.userType = userType;
        this.roles = roles;
        this.resendInvite = resendInvite;
    }

    public void setStatus(IdamStatus status) {
        if (null != status) {
            this.status = status;
        }
    }
}
