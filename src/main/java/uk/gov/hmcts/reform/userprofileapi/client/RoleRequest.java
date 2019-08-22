package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class RoleRequest {
    @JsonProperty
    private List<Map> roles;
}
