package uk.gov.hmcts.reform.userprofileapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;


@RunWith(MockitoJUnitRunner.class)
public class IdamServiceExceptionTest {

    @Test
    public void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        IdamServiceException exception = new IdamServiceException(message, HttpStatus.NOT_FOUND);

        assertThat(exception).hasMessage(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
