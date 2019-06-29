package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class UpdateUserProfileData implements RequestData {

    @Email(regexp = "\\A(?=[a-zA-Z0-9@.!#$%&'*+/=?^_`{|}~-]{6,254}\\z)(?=[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]{1,64}@)[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:(?=[a-zA-Z0-9-]{1,63}\\.)[a-zA-Z0-9](?:[a-z0-9-]*[a-zA-Z0-9])?\\.)+(?=[a-zA-Z0-9-]{1,63}\\z)[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\\z")
    @NotBlank (message = "email must not be null or blank")
    @Size(max = 255)
    private String email;

    @NotBlank (message = "firstName must not be null or blank")
    @Size(max = 255)
    private String firstName;

    @NotBlank (message = "lastName must not be null or blank")
    @Size(max = 255)
    private String lastName;

    @NotBlank (message = "status must not be null or blank")
    private String idamStatus;

    @JsonCreator
    public UpdateUserProfileData(@JsonProperty(value = "emailAddress") String email,
                                 @JsonProperty(value = "firstName") String firstName,
                                 @JsonProperty(value = "lastName") String lastName,
                                 @JsonProperty(value = "idamStatus") String idamStatus) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idamStatus = idamStatus;
    }

}
