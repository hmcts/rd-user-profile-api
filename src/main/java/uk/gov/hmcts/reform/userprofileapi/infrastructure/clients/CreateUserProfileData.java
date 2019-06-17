package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotNull;

import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;

import java.util.List;

public class CreateUserProfileData implements RequestData {

    @NotNull
    private String email;

    @NotNull
    private String firstName;

    @NotNull
    private String lastName;

    @NotNull
    private String userCategory;

    @NotNull
    private String userType;

    @NotNull
    private List<String> roles;

    @JsonCreator
    public CreateUserProfileData(@JsonProperty(value = "email") String email,
                                 @JsonProperty(value = "firstName") String firstName,
                                 @JsonProperty(value = "lastName") String lastName,
                                 @JsonProperty(value = "userCategory") String userCategory,
                                 @JsonProperty(value = "userType") String userType,
                                 @JsonProperty(value = "roles") List<String> idamRoles) {

        if (email == null) {
            throw new RequiredFieldMissingException("email must not be null");
        } /*else if(email != null) {
            String regEx = "^[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+(?:\\\\.[a-zA-Z0-9_!#$%&’*+/=?`{|}~^-]+)*@[a-zA-Z0-9-]+(?:\\\\.[a-zA-Z0-9-]+)*$";
            if(!email.matches(regEx)){
                throw new RequiredFieldMissingException("email is not valid");
            }
        } */else if (firstName == null) {
            throw new RequiredFieldMissingException("firstName must not be null");
        } else if (lastName == null) {
            throw new RequiredFieldMissingException("lastName must not be null");
        } else if (userCategory == null) {
            throw new RequiredFieldMissingException("userCategory must not be null");
        } else if (userType == null) {
            throw new RequiredFieldMissingException("userType must not be null");
        } else if (CollectionUtils.isEmpty(idamRoles)) {
            throw new RequiredFieldMissingException("at least one role required");
        }

        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userCategory = userCategory;
        this.userType = userType;
        this.roles = idamRoles;
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

    public String getUserCategory() {
        return userCategory;
    }

    public String getUserType() {
        return userType;
    }

    public List<String> getIdamRoles() {
        return roles;
    }
}
