package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;

class UserProfileRolesResponseTest {

    @Test
    void test_AddAndDeleteRoleResponse() {
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse(ResponseEntity.status(OK).build());
        roleAdditionResponse.setIdamMessage("Success");

        RoleDeletionResponse deletionResponse = new RoleDeletionResponse();
        deletionResponse.setIdamStatusCode(OK.toString());
        deletionResponse.setIdamMessage("success");
        List<RoleDeletionResponse> roleDeletionResponseData = List.of(deletionResponse);

        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setRoleDeletionResponse(roleDeletionResponseData);
        userProfileResponse.setRoleAdditionResponse(roleAdditionResponse);

        assertThat(userProfileResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileResponse.getRoleAdditionResponse().getIdamMessage()).isEqualTo("Success");
    }

    @Test
    void test_AddRolesViaConstructor() {
        RoleAdditionResponse roleAdditionResponse = new RoleAdditionResponse(ResponseEntity.status(OK).build());

        RoleDeletionResponse deletionResponse = new RoleDeletionResponse("pui-case-manager",
                ResponseEntity.status(OK).build());
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
