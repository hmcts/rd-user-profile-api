package uk.gov.hmcts.reform.userprofileapi.clients;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class IdamUserResponse {
    private Boolean active;
    private String email;
    private String forename;
    private String id;
    private Boolean locked;
    private List<String> roles;
    private String surname;

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
}
