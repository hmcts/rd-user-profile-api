package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class RoleDeletionResponseTest {

    @Test
    public void testDeleteRoleResponse() {
        final String caseWorker = "caseworker";
        final HttpStatus status = HttpStatus.OK;

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse(caseWorker, status);

        assertThat(roleDeletionResponse.getRoleName()).isEqualTo(caseWorker);
        assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(String.valueOf(status.value()));
        assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo(resolveStatusAndReturnMessage(status));
    }
}