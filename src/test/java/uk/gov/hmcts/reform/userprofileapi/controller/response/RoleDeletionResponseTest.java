package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

class RoleDeletionResponseTest {

    @Test
    void test_DeleteRoleResponse() {
        final String caseWorker = "caseworker";

        RoleDeletionResponse roleDeletionResponse = new RoleDeletionResponse(caseWorker,
                ResponseEntity.status(OK).build());

        assertThat(roleDeletionResponse.getRoleName()).isEqualTo(caseWorker);
        assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(String.valueOf(OK.value()));
        assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo(resolveStatusAndReturnMessage(OK));
    }
}
