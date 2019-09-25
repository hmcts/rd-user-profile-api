package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant;

public class DeleteRoleResponseTest {


    @Test
    public void should_Return_Delete_Role_Resposne() {

        DeleteRoleResponse deleteRoleResponse = new DeleteRoleResponse(UserProfileConstant.CASE_WORKER,HttpStatus.OK);
        assertThat(deleteRoleResponse.getRoleName()).isEqualTo(UserProfileConstant.CASE_WORKER);
        assertThat(deleteRoleResponse.getIdamStatusCode()).isEqualTo("200");
        assertThat(deleteRoleResponse.getIdamMessage()).isEqualTo("11 OK");
    }
}