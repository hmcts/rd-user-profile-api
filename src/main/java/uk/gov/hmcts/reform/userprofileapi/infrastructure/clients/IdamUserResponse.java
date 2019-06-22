package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;

public class IdamUserResponse {
    private Boolean active;
    @NotNull
    private String email;
    private String forename;
    private String id;

    @JsonCreator
    public IdamUserResponse(@JsonProperty(value = "active") Boolean active,
                               @NotNull @JsonProperty(value = "email") String email,
                               @JsonProperty(value = "forename") String forename,
                               @JsonProperty(value = "id") String id,
                               @JsonProperty(value = "locked") Boolean locked,
                               @JsonProperty(value = "roles") List<String> roles,
                               @JsonProperty(value = "surname") String surname) {
        this.active = active;
        this.email = email;
        this.forename = forename;
        this.id = id;
        this.locked = locked;
        this.roles = roles;
        this.surname = surname;
    }

    private Boolean locked;
    private List<String> roles;
    private String surname;

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getForename() {
        return forename;
    }

    public void setForename(String forename) {
        this.forename = forename;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Boolean getLocked() {
        return locked;
    }

    public void setLocked(Boolean locked) {
        this.locked = locked;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }
}
