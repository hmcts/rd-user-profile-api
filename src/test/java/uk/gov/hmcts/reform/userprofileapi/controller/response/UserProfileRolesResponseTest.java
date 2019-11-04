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
        RoleDeletionResponse deletionResponse = new RoleDeletionResponse();
        deletionResponse.setIdamStatusCode(HttpStatus.OK.toString());
        deletionResponse.setIdamMessage("success");
        List<RoleDeletionResponse> roleDeletionResponseData = new ArrayList<>();
        roleDeletionResponseData.add(deletionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setRoleDeletionResponse(roleDeletionResponseData);
        userProfileResponse.setRoleAdditionResponse(roleAdditionResponse);

        assertThat(userProfileResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200 OK");
        assertThat(userProfileResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("Success");
    }

    @Test
    public void testAddRolesViaConstructor() {

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse(HttpStatus.OK);

        RoleDeletionResponse deletionResponse = new RoleDeletionResponse("pui-case-manager",HttpStatus.OK);
        List<RoleDeletionResponse> roleDeletionResponseData = new ArrayList<>();
        roleDeletionResponseData.add(deletionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setRoleAdditionResponse(roleAdditionResponse);
        userProfileResponse.setRoleDeletionResponse(roleDeletionResponseData);

        assertThat(userProfileResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("11 OK");

        assertThat(userProfileResponse.getRoleDeletionResponse()).isNotNull();
        assertThat(userProfileResponse.getRoleDeletionResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(userProfileResponse.getRoleDeletionResponse().get(0).getIdamMessage()).isEqualTo("11 OK");
        assertThat(userProfileResponse.getRoleDeletionResponse().get(0).getIdamStatusCode()).isEqualTo("200");

    }
}