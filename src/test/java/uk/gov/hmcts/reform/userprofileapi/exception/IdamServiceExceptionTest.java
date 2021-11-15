package uk.gov.hmcts.reform.userprofileapi.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class IdamServiceExceptionTest {

    @Test
    void testCreateExceptionCorrectly() {
        String message = "this-is-a-test-message";
        IdamServiceException exception = new IdamServiceException(message, HttpStatus.NOT_FOUND);

        assertThat(exception)
                .hasMessage(message)
                .isInstanceOf(RuntimeException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
