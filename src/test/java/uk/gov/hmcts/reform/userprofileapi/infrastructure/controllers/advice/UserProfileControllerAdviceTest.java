package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerAdviceTest {

    private UserProfileControllerAdvice advice = new UserProfileControllerAdvice();

    @Test
    public void should_handle_required_field_missing_exception() {

        String message = "test-ex-message";
        RequiredFieldMissingException ex = mock(RequiredFieldMissingException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(ex.getMessage()).thenReturn(message);

        ResponseEntity response = advice.handleRequiredFieldMissingException(request, ex);

        assertThat(response).isEqualToComparingFieldByField(expected);

    }

}
