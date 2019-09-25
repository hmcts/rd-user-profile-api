package uk.gov.hmcts.reform.userprofileapi.client;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileRolesResponse {


    private AddRoleResponse addRoleResponse;
    private List<DeleteRoleResponse> deleteRolesResponse;

    public UserProfileRolesResponse(AddRoleResponse addRoleResponse, List<DeleteRoleResponse> deleteResponses) {
        this.addRoleResponse = addRoleResponse;
        this.deleteRolesResponse = deleteResponses;
    }
}