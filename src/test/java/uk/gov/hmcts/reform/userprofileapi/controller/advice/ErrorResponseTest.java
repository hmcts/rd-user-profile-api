package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import static java.time.LocalTime.now;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ErrorResponseTest {

    @Test
    public void test_builder() {
        final String errorMsg = "Some error";
        final String desc = "Some desc";
        final String tmstp = now().toString();
        ErrorResponse sut = ErrorResponse.builder()
                .errorMessage(errorMsg)
                .errorDescription(desc)
                .timeStamp(tmstp)
                .build();

        assertThat(sut.getErrorMessage()).isEqualTo(errorMsg);
        assertThat(sut.getErrorDescription()).isEqualTo(desc);
        assertThat(sut.getTimeStamp()).isEqualTo(tmstp);
    }
}