package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UserProfileRolesResponseTest {


    @Test
    public void should_Return_User_profile_Response_With_Setters() {

        AttributeResponse attributeResponse = new AttributeResponse();
        attributeResponse.setIdamStatusCode(HttpStatus.OK.toString());
        attributeResponse.setIdamMessage("Success");
        AddRoleResponse addRoleResponse = new AddRoleResponse();
        addRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        addRoleResponse.setIdamMessage("Success");
        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse();
        deleteRoleResponse.setIdamStatusCode(HttpStatus.OK.toString());
        deleteRoleResponse.setIdamMessage("success");
        List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<>();
        deleteRoleResponses.add(deleteRoleResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(attributeResponse,addRoleResponse,deleteRoleResponses);

        userProfileRolesResponse.setAttributeResponse(attributeResponse);
        assertThat(userProfileRolesResponse.getAttributeResponse().getIdamStatusCode()).isEqualTo("200 OK");
        assertThat(userProfileRolesResponse.getAttributeResponse().getIdamMessage()).isEqualTo("Success");
        userProfileRolesResponse.setAddRolesResponse(addRoleResponse);
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200 OK");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("Success");
        userProfileRolesResponse.setDeleteRolesResponse(deleteRoleResponses);
        userProfileRolesResponse.getDeleteRolesResponse().forEach(
                deleteRoleResponse1 -> {

                    assertThat(deleteRoleResponse1.getIdamMessage()).isEqualTo("Success");
                    assertThat(deleteRoleResponse1.getIdamStatusCode()).isEqualTo("200 OK");
                }
        );


    }

    @Test
    public void should_Return_User_profile_Response_With_Constructor() {

        AttributeResponse attributeResponse = new AttributeResponse(HttpStatus.OK);

        AddRoleResponse addRoleResponse = new AddRoleResponse(HttpStatus.OK);

        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse("pui-case-manager",HttpStatus.OK);
        List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<>();
        deleteRoleResponses.add(deleteRoleResponse);

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(attributeResponse,addRoleResponse,deleteRoleResponses);

        assertThat(userProfileRolesResponse.getAttributeResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamStatusCode()).isEqualTo("200");
        assertThat(userProfileRolesResponse.getAddRolesResponse().getIdamMessage()).isEqualTo("11 OK");

        assertThat(userProfileRolesResponse.getDeleteRolesResponse()).isNotNull();
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getRoleName()).isEqualTo("pui-case-manager");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamMessage()).isEqualTo("11 OK");
        assertThat(userProfileRolesResponse.getDeleteRolesResponse().get(0).getIdamStatusCode()).isEqualTo("200");

    }
}