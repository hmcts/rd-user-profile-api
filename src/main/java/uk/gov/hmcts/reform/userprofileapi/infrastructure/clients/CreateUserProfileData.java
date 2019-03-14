package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import javax.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public class CreateUserProfileData implements RequestData {

    @NotNull private String email;
    @NotNull private String firstName;
    @NotNull private String lastName;

    public CreateUserProfileData() {
    }

    public CreateUserProfileData(String email, String firstName, String lastName) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
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
