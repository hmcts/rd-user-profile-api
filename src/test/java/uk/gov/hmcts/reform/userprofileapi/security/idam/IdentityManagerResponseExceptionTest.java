package uk.gov.hmcts.reform.userprofileapi.security.idam;

import static org.mockito.Mockito.mock;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.security.idam.IdentityManagerResponseException;

@RunWith(MockitoJUnitRunner.class)
public class IdentityManagerResponseExceptionTest {

    @Test
    public void should_create_exception_successfully() {
        String message = "test-exception-message";
        HttpClientErrorException cause = mock(HttpClientErrorException.class);
        IdentityManagerResponseException ex = new IdentityManagerResponseException(message, cause);
        Assertions.assertThat(ex).hasMessage(message);
        Assertions.assertThat(ex).hasCause(cause);

    }

}
