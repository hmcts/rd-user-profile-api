package uk.gov.hmcts.reform.userprofileapi.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;

@RunWith(MockitoJUnitRunner.class)
public class RequiredFieldMissingExceptionTest {

    @Test
    public void test_create_exception_correctly() {
        String message = "this-is-a-test-message";
        RequiredFieldMissingException exception = new RequiredFieldMissingException(message);

        assertThat(exception).hasMessage(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);


    }



}
