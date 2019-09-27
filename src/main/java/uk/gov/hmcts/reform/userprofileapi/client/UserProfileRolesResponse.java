package uk.gov.hmcts.reform.userprofileapi.client;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileRolesResponse {


    private AddRoleResponse addRolesResponse;
    private List<DeleteRoleResponse> deleteRolesResponse;

    public UserProfileRolesResponse(AddRoleResponse addRolesResponse, List<DeleteRoleResponse> deleteRolesResponse) {
        this.addRolesResponse = addRolesResponse;
        this.deleteRolesResponse = deleteRolesResponse;
    }
}