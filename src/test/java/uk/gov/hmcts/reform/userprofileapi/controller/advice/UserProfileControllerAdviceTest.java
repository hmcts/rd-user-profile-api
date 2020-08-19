package uk.gov.hmcts.reform.userprofileapi.controller.advice;

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
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileControllerAdviceTest {

    private UserProfileControllerAdvice advice = new UserProfileControllerAdvice();

    @Test
    public void test_handle_required_field_missing_exception() {
        String message = "test-ex-message";
        RequiredFieldMissingException exception = new RequiredFieldMissingException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity response = advice.handleRequiredFieldMissingException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_handle_invalid_request_exception() {
        String message = "test-ex-message";
        InvalidRequest exception = new InvalidRequest(message);

        ResponseEntity response = advice.customValidationError(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_return_404_when_resource_not_found_exception() {
        String message = "test-ex-message";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity response = advice.handleResourceNotFoundException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void test_return_400_when_invalid_method_argument() {
        String message = "test-ex-message";
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(exception.getMessage()).thenReturn(message);

        ResponseEntity response = advice.handleMethodArgumentNotValidException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_return_400_when_duplicate_email() {
        String message = "test-ex-message";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity response = advice.handleDataIntegrityViolationException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_handle() {
        String message = "test-ex-message";
        IdamServiceException exception = new IdamServiceException(message, HttpStatus.BAD_REQUEST);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity response = advice.handleIdamServiceException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_return_500_when_unhandled_exception() {
        String message = "test-ex-message";
        Exception ex = mock(Exception.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(ex.getMessage()).thenReturn(message);

        ResponseEntity response = advice.handleUnknownException(request, ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    public void test_return_400_when_httpMessageConversionException() {
        String message = "test-ex-message";
        HttpMessageConversionException ex = new HttpMessageConversionException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity response = advice.handleHttpMessageConversionException(request, ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void test_return_throwable_when_getRootCause() {

        Throwable throwableMock = mock(Throwable.class);
        when(throwableMock.getCause()).thenReturn(new Throwable());

        Throwable result = UserProfileControllerAdvice.getRootException(throwableMock);
        assertThat(result).isNotNull();
    }

    @Test
    public void test_return_404_when_too_many_request_exception() {
        String message = "test-ex-message";
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity response = advice.handleTooManyRequestsException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
