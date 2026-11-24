package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.exception.ForbiddenException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;

import java.util.Collections;
import java.util.LinkedList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserProfileControllerAdviceTest {


    @Mock
    HttpMessageNotReadableException httpMessageNotReadableException;

    @Mock
    LinkedList<JsonMappingException.Reference> path = new LinkedList<>();

    private final UserProfileControllerAdvice advice = new UserProfileControllerAdvice();

    @Test
    void test_handle_required_field_missing_exception() {
        String message = "test-ex-message";
        RequiredFieldMissingException exception = new RequiredFieldMissingException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleRequiredFieldMissingException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_handle_invalid_request_exception() {
        String message = "test-ex-message";
        InvalidRequest exception = new InvalidRequest(message);

        ResponseEntity<Object> response = advice.customValidationError(exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_handle_invalid_serialization_exception() {

        JsonMappingException jm = mock(JsonMappingException.class);
        JsonMappingException.Reference rf = mock(JsonMappingException.Reference.class);
        when(httpMessageNotReadableException.getCause()).thenReturn(jm);
        when(jm.getPath()).thenReturn(Collections.unmodifiableList(path));
        when(jm.getPath().get(0)).thenReturn(rf);
        when(jm.getPath().get(0).getFieldName()).thenReturn("field");
        ResponseEntity<Object> responseEntity = advice
            .customSerializationError(httpMessageNotReadableException);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());

    }

    @Test
    void test_handle_missing_request_exception() {
        MissingServletRequestParameterException
            exception = new MissingServletRequestParameterException("Invalid Request","Missing Parameter");

        ResponseEntity<Object> responseEntity = advice.handleMissingRequestParameter(exception);

        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertEquals(exception.getMessage(), ((ErrorResponse) responseEntity.getBody())
            .getErrorDescription());

    }


    @Test
    void test_return_404_when_resource_not_found_exception() {
        String message = "test-ex-message";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleResourceNotFoundException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void test_return_400_when_invalid_method_argument() {
        String message = "test-ex-message";
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        when(exception.getMessage()).thenReturn(message);

        ResponseEntity<Object> response = advice.handleMethodArgumentNotValidException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_return_400_when_duplicate_email() {
        String message = "test-ex-message";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleDataIntegrityViolationException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_handle() {
        String message = "test-ex-message";
        IdamServiceException exception = new IdamServiceException(message, HttpStatus.BAD_REQUEST);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleIdamServiceException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_return_500_when_unhandled_exception() {
        String message = "test-ex-message";
        Exception ex = mock(Exception.class);
        HttpServletRequest request = mock(HttpServletRequest.class);

        // mockito reports unnecessary stubbing and 'strict' mock are enabled, remove
        // when(ex.getMessage()).thenReturn(message);

        ResponseEntity<Object> response = advice.handleUnknownException(request, ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void test_return_400_when_httpMessageConversionException() {
        String message = "test-ex-message";
        HttpMessageConversionException ex = new HttpMessageConversionException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleHttpMessageConversionException(request, ex);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void test_return_throwable_when_getRootCause() {

        Throwable throwableMock = mock(Throwable.class);
        when(throwableMock.getCause()).thenReturn(new Throwable());

        Throwable result = UserProfileControllerAdvice.getRootException(throwableMock);
        assertThat(result).isNotNull();
    }

    @Test
    void test_return_404_when_too_many_request_exception() {
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.TOO_MANY_REQUESTS);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleTooManyRequestsException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }

    @Test
    void test_return_403_when_LD_forbidden_exception() {
        String message = "feature flag is not released";

        ForbiddenException exception = new ForbiddenException(message);
        HttpServletRequest request = mock(HttpServletRequest.class);

        ResponseEntity<Object> response = advice.handleForbiddenException(request, exception);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }
}
