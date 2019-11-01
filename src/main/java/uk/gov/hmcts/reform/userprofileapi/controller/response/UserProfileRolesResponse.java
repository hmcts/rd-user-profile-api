package uk.gov.hmcts.reform.userprofileapi.controller.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileRolesResponse {


    private AttributeResponse attributeResponse;
    private RoleAdditionResponse roleAdditionResponse;
    private List<RoleDeletionResponse> roleDeletionResponse;

    public UserProfileRolesResponse(AttributeResponse attributeResponse, RoleAdditionResponse addRolesResponse, List<RoleDeletionResponse> deleteRolesResponse) {
        this.attributeResponse = attributeResponse;
        this.roleAdditionResponse = addRolesResponse;
        this.roleDeletionResponse = deleteRolesResponse;
    }
}