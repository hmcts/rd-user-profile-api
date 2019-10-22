package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.constant.UserProfileConstant;

public class RoleDeletionResponseTest {

    @Test
    public void testDeleteRoleResponse() {
        final HttpStatus status = HttpStatus.OK;
        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse(UserProfileConstant.CASE_WORKER, status);
        assertThat(roleDeletionResponse.getRoleName()).isEqualTo(UserProfileConstant.CASE_WORKER);
        assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(String.valueOf(status.value()));
        assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo(resolveStatusAndReturnMessage(status));
    }
}