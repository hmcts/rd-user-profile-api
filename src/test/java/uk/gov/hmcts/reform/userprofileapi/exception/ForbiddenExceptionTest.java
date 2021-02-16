package uk.gov.hmcts.reform.userprofileapi.exception;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ForbiddenExceptionTest {

    @Test
    public void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        ForbiddenException exception = new ForbiddenException(message);

        assertThat(exception)
                .hasMessage(message)
                .isInstanceOf(ForbiddenException.class);
    }
}