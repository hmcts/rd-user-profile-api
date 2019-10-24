package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class RoleDeletionResponseTest {

    @Test
    public void testDeleteRoleResponse() {
        final String CASE_WORKER = "caseworker";
        final HttpStatus status = HttpStatus.OK;
        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse(CASE_WORKER, status);
        assertThat(roleDeletionResponse.getRoleName()).isEqualTo(CASE_WORKER);
        assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(String.valueOf(status.value()));
        assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo(resolveStatusAndReturnMessage(status));
    }
}