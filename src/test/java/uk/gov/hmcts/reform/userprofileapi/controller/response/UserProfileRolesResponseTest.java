package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UserProfileRolesResponseTest {

    @Test
    public void testAddAndDeleteRoleResponse() {
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse();
        roleAdditionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleAdditionResponse.setIdamMessage("Success");
        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse();
        roleDeletionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        roleDeletionResponse.setIdamMessage("success");
        List<RoleDeletionResponse> roleDeletionRespons = new ArrayList<>();
        roleDeletionRespons.add(roleDeletionResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(roleAdditionResponse, roleDeletionRespons);

        userProfileRolesResponse.setAddRolesResponse(roleAdditionResponse);
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200 OK");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("Success");
    }

    @Test
    public void testAddRolesViaConstructor() {

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse(HttpStatus.OK);

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse("pui-case-manager",HttpStatus.OK);
        List<RoleDeletionResponse> roleDeletionRespons = new ArrayList<>();
        roleDeletionRespons.add(roleDeletionResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(roleAdditionResponse, roleDeletionRespons);

        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("11 OK");

        assertThat(userProfileRolesResponse.getDeleteRolesResponse()).isNotNull();
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamMessage()).isEqualTo("11 OK");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");

    }
}