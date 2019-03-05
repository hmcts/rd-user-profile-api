package uk.gov.hmcts.reform.userprofileapi.domain.entities;

public class UserProfile {
    private long id;
    private String idamId;
    private String firstName;
    private String lastName;

    public UserProfile() {
    }

    public UserProfile(long id, String idamId, String firstName, String lastName) {
        this.id = id;
        this.idamId = idamId;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public String getIdamId() {
        return idamId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }
}
