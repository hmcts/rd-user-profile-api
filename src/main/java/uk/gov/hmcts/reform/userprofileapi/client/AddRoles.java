package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

import lombok.Getter;

@Getter
public class AddRoles implements RequestData {

    @JsonProperty
    private final List<RoleName> roles;

    @JsonCreator
    public AddRoles(@JsonProperty("roles") List<RoleName> roles) {

        this.roles = roles;
    }
}
