package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice.ErrorConstants.DATA_INTEGRITY_VIOLATION;
import static uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice.ErrorConstants.UNKNOWN_EXCEPTION;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class UserProfileControllerAdvice {

    private static final String LOG_STRING = "handling exception: {}";

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<Object> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        return errorDetailsResponseEntity(e, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(
        HttpServletRequest request,
        MethodArgumentNotValidException e
    ) {
        return errorDetailsResponseEntity(e, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<Object> handleResourceNotFoundException(
        HttpServletRequest request,
        ResourceNotFoundException e
    ) {
        return errorDetailsResponseEntity(e, NOT_FOUND, RESOURCE_NOT_FOUND.getErrorMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleDataIntegrityViolationException(
        HttpServletRequest request,
        DataIntegrityViolationException e
    ) {
        return errorDetailsResponseEntity(e, BAD_REQUEST, DATA_INTEGRITY_VIOLATION.getErrorMessage());
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    protected ResponseEntity<Object> handleHttpMessageConversionException(
        HttpServletRequest request,
        HttpMessageConversionException e
    ) {
        return errorDetailsResponseEntity(e, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(IdamServiceException.class)
    protected ResponseEntity<Object> handleIdamServiceException(
            HttpServletRequest request,
            IdamServiceException e
    ) {
        return errorDetailsResponseEntity(e, e.getHttpStatus(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleUnknownException(
        HttpServletRequest request,
        Exception e
    ) {
        return errorDetailsResponseEntity(e, INTERNAL_SERVER_ERROR, UNKNOWN_EXCEPTION.getErrorMessage());
    }

    public String getTimeStamp() {
        return new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSS", Locale.ENGLISH).format(new Date());
    }

    public static Throwable getRootException(Throwable exception) {
        Throwable rootException = exception;
        while (rootException.getCause() != null) {
            rootException = rootException.getCause();
        }
        return rootException;
    }

    private ResponseEntity<Object> errorDetailsResponseEntity(Exception ex, HttpStatus httpStatus, String errorMsg) {

        log.error(LOG_STRING, ex.getMessage(), ex);
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorMessage(errorMsg)
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, httpStatus);
    }
}
