package uk.gov.hmcts.reform.userprofileapi.controller.response;

import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserProfileRolesResponse {
    private RoleAdditionResponse addRolesResponse;
    private List<RoleDeletionResponse> deleteRolesResponse;

    public UserProfileRolesResponse(RoleAdditionResponse addRolesResponse, List<RoleDeletionResponse> deleteRolesResponse) {
        this.addRolesResponse = addRolesResponse;
        this.deleteRolesResponse = deleteRolesResponse;
    }
}