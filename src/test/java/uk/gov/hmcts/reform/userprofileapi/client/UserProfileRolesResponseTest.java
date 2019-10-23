package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleAdditionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;

public class UserProfileRolesResponseTest {


    @Test
    public void should_Return_User_profile_Response_With_Setters() {

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
    public void should_Return_User_profile_Response_With_Constructor() {

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