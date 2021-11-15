package uk.gov.hmcts.reform.userprofileapi.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class UndefinedExceptionTest {

    @Test
    void testCreateExceptionCorrectly() {
        String message = "this-is-a-test-message";
        UndefinedException exception = new UndefinedException(message);

        assertThat(exception)
                .hasMessage(message)
                .isInstanceOf(UndefinedException.class);
    }

}
