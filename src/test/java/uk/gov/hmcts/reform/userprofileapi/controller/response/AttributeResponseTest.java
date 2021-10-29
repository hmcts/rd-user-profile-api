package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

class AttributeResponseTest {

    private final HttpStatus httpStatus = HttpStatus.OK;

    private final AttributeResponse sut = new AttributeResponse(status(OK).build());

    @Test
    void test_AttributeResponse() {
        final Integer expectedIdamStatusCode = httpStatus.value();
        final String expectedIdamMessage = resolveStatusAndReturnMessage(httpStatus);

        assertThat(sut.getIdamStatusCode()).isEqualTo(expectedIdamStatusCode);
        assertThat(sut.getIdamMessage()).isEqualTo(expectedIdamMessage);
    }

}
