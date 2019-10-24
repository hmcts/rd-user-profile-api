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
        userProfileResponse.setDeleteRolesResponse(roleDeletionResponseData);
        userProfileResponse.setAddRolesResponse(roleAdditionResponse);

        assertThat(userProfileResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200 OK");
        assertThat(userProfileResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("Success");
    }

    @Test
    public void testAddRolesViaConstructor() {

        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse(HttpStatus.OK);

        RoleDeletionResponse deletionResponse = new RoleDeletionResponse("pui-case-manager",HttpStatus.OK);
        List<RoleDeletionResponse> roleDeletionResponseData = new ArrayList<>();
        roleDeletionResponseData.add(deletionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setAddRolesResponse(roleAdditionResponse);
        userProfileResponse.setDeleteRolesResponse(roleDeletionResponseData);

        assertThat(userProfileResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("11 OK");

        assertThat(userProfileResponse.getDeleteRolesResponse()).isNotNull();
        assertThat(userProfileResponse.getDeleteRolesResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(userProfileResponse.getDeleteRolesResponse().get(0).getIdamMessage()).isEqualTo("11 OK");
        assertThat(userProfileResponse.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");

    }
}