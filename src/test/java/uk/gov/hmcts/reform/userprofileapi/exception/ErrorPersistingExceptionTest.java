package uk.gov.hmcts.reform.userprofileapi.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ErrorPersistingExceptionTest {

    @Test
    void testCreateExceptionCorrectly() {
        String message = "this-is-a-test-message";
        ErrorPersistingException exception = new ErrorPersistingException(message);

        assertThat(exception)
                .hasMessage(message)
                .isInstanceOf(ErrorPersistingException.class);
    }
}
