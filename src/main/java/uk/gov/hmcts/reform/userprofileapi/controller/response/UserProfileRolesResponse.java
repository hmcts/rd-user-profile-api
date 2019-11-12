package uk.gov.hmcts.reform.userprofileapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileRolesResponse {


    @JsonProperty("updateStatusResponse")
    private AttributeResponse attributeResponse;
    private RoleAdditionResponse roleAdditionResponse;
    private List<RoleDeletionResponse> roleDeletionResponse;

    public UserProfileRolesResponse(RoleAdditionResponse roleAdditionResponse, List<RoleDeletionResponse> roleDeletionResponse) {
        this.roleAdditionResponse = roleAdditionResponse;
        this.roleDeletionResponse = roleDeletionResponse;
    }
}