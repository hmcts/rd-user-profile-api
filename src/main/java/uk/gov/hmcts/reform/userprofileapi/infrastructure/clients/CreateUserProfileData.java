package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;

public class CreateUserProfileData implements RequestData {

    @NotNull
    private String email;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;
    private String languagePreference;

    private boolean emailCommsConsent;
    private boolean postalCommsConsent;

    @NotNull
    private String userCategory;

    @NotNull
    private String userType;
    private String idamRoles;

    @JsonCreator
    public CreateUserProfileData(@JsonProperty(value = "email") String email,
                                 @JsonProperty(value = "firstName") String firstName,
                                 @JsonProperty(value = "lastName") String lastName,
                                 @JsonProperty(value = "languagePreference") String languagePreference,
                                 @JsonProperty(value = "emailCommsConsent") boolean emailCommsConsent,
                                 @JsonProperty(value = "postalCommsConsent") boolean postalCommsConsent,
                                 @JsonProperty(value = "userCategory") String userCategory,
                                 @JsonProperty(value = "userType") String userType,
                                 @JsonProperty(value = "idamRoles") String idamRoles) {

        if (email == null) {
            throw new RequiredFieldMissingException("email must not be null");
        } else if (firstName == null) {
            throw new RequiredFieldMissingException("firstName must not be null");
        } else if (lastName == null) {
            throw new RequiredFieldMissingException("lastName must not be null");
        } else if (userCategory == null) {
            throw new RequiredFieldMissingException("userCategory must not be null");
        } else if (userType == null) {
            throw new RequiredFieldMissingException("userType must not be null");
        }

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.languagePreference = languagePreference;
        this.emailCommsConsent = emailCommsConsent;
        this.postalCommsConsent = postalCommsConsent;
        this.userCategory = userCategory;
        this.userType = userType;
        this.idamRoles = idamRoles;
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

    public String getLanguagePreference() {
        return languagePreference;
    }

    public boolean isEmailCommsConsent() {
        return emailCommsConsent;
    }

    public boolean isPostalCommsConsent() {
        return postalCommsConsent;
    }

    public String getUserCategory() {
        return userCategory;
    }

    public String getUserType() {
        return userType;
    }

    public String getIdamRoles() {
        return idamRoles;
    }
}
