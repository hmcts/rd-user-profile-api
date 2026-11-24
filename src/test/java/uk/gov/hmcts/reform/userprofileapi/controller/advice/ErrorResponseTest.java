package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static java.time.LocalTime.now;
import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    @Test
    void test_builder() {
        final String errorMsg = "Some error";
        final String desc = "Some desc";
        final String tmstp = now().toString();
        ErrorResponse sut = ErrorResponse.builder()
                .errorMessage(errorMsg)
                .errorDescription(desc)
                .timeStamp(tmstp)
                .status(HttpStatus.UNAUTHORIZED.value())
                .build();

        assertThat(sut.getErrorMessage()).isEqualTo(errorMsg);
        assertThat(sut.getErrorDescription()).isEqualTo(desc);
        assertThat(sut.getTimeStamp()).isEqualTo(tmstp);
        assertThat(sut.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }
}
