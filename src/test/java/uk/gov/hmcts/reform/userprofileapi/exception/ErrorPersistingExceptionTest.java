package uk.gov.hmcts.reform.userprofileapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ErrorPersistingExceptionTest {

    @Test
    public void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        ErrorPersistingException exception = new ErrorPersistingException(message);

        assertThat(exception).hasMessage(message);
        assertThat(exception).isInstanceOf(ErrorPersistingException.class);
    }
}
