package uk.gov.hmcts.reform.userprofileapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class UndefinedExceptionTest {

    @Test
    public void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        UndefinedException exception = new UndefinedException(message);

        assertThat(exception).hasMessage(message);
        assertThat(exception).isInstanceOf(UndefinedException.class);
    }

}
