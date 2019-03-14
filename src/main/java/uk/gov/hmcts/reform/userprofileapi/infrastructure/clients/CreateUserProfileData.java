package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import org.springframework.stereotype.Component;

@Component
public class CreateUserProfileData implements RequestData {

    private String email;
    private String firstName;
    private String lastName;

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
