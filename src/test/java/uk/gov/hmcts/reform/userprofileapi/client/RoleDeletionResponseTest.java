package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;

public class RoleDeletionResponseTest {


    @Test
    public void should_Return_Delete_Role_Resposne() {

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse(UserProfileConstant.CASE_WORKER,HttpStatus.OK);
        assertThat(roleDeletionResponse.getRoleName()).isEqualTo(UserProfileConstant.CASE_WORKER);
        assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo("200");
        assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo("11 OK");
    }
}