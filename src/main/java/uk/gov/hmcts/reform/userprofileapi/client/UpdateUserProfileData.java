package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileData implements RequestData {

    private String email;

    private String firstName;

    private String lastName;

    private String idamStatus;

    @JsonCreator
    public UpdateUserProfileData(@JsonProperty(value = "email") String email,
                                 @JsonProperty(value = "firstName") String firstName,
                                 @JsonProperty(value = "lastName") String lastName,
                                 @JsonProperty(value = "idamStatus") String idamStatus) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.idamStatus = idamStatus;
    }

}
