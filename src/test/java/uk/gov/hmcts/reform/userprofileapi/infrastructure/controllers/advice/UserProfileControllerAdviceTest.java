package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;

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

    @Test
    public void should_return_404_when_resource_not_found_exception() {
        String message = "test-ex-message";
        ResourceNotFoundException ex = mock(ResourceNotFoundException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.NOT_FOUND);

        when(ex.getMessage()).thenReturn(message);

        ResponseEntity response = advice.handleResourceNotFoundException(request, ex);

        assertThat(response).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void should_return_400_when_invalid_method_argument() {
        String message = "test-ex-message";
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(ex.getMessage()).thenReturn(message);

        ResponseEntity response = advice.handleMethodArgumentNotValidException(request, ex);

        assertThat(response).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void should_return_400_when_duplicate_email() {
        String message = "test-ex-message";
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        when(ex.getMessage()).thenReturn(message);

        ResponseEntity response = advice.handleDataIntegrityViolationException(request, ex);

        assertThat(response).isEqualToComparingFieldByField(expected);

    }

    @Test
    public void should_return_500_when_unhandled_exception() {
        String message = "test-ex-message";
        Exception ex = mock(Exception.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        ResponseEntity<String> expected = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(ex.getMessage()).thenReturn(message);

        ResponseEntity response =  advice.handleUnknownException(request, ex);

        assertThat(response).isEqualToComparingFieldByField(expected);
    }


}
