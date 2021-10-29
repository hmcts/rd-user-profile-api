package uk.gov.hmcts.reform.userprofileapi.exception;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ForbiddenExceptionTest {

    @Test
    void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        ForbiddenException exception = new ForbiddenException(message);

        assertThat(exception)
                .hasMessage(message)
                .isInstanceOf(ForbiddenException.class);
    }
}
