package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UserProfileRolesResponseTest {


    @Test
    public void should_Return_User_profile_Response_With_Setters() {

        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        addRoleResponse.setIdamMessage("Success");
        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse();
        deleteRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        deleteRoleResponse.setIdamMessage("success");
        List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<>();
        deleteRoleResponses.add(deleteRoleResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(addRoleResponse,deleteRoleResponses);

        userProfileRolesResponse.setAddRolesResponse(addRoleResponse);
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200 OK");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("Success");
    }

    @Test
    public void should_Return_User_profile_Response_With_Constructor() {

        AddRoleResponse addRoleResponse = new AddRoleResponse(HttpStatus.OK);

        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse("pui-case-manager",HttpStatus.OK);
        List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<>();
        deleteRoleResponses.add(deleteRoleResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(addRoleResponse,deleteRoleResponses);

        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("11 OK");

        assertThat(userProfileRolesResponse.getDeleteRolesResponse()).isNotNull();
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamMessage()).isEqualTo("11 OK");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");

    }
}