package uk.gov.hmcts.reform.userprofileapi.controller.advice;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.DATA_INTEGRITY_VIOLATION;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.RESOURCE_NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorConstants.UNKNOWN_EXCEPTION;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;

@Slf4j
@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.userprofileapi.controller")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class UserProfileControllerAdvice {

    private static final String LOG_STRING = "handling exception: {}";

    @Value("${resendInterval}")
    private String resendInterval;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<Object> handleRequiredFieldMissingException(
            HttpServletRequest request,
            RequiredFieldMissingException e
    ) {
        return errorDetailsResponseEntity(e, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }

    @ExceptionHandler(InvalidRequest.class)
    public ResponseEntity<Object> customValidationError(
            InvalidRequest ex) {
        return errorDetailsResponseEntity(ex, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValidException(
            HttpServletRequest request,
            MethodArgumentNotValidException e
    ) {
        return patternErrorDetailsResponseEntity(e, BAD_REQUEST, INVALID_REQUEST.getErrorMessage());
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

    @ExceptionHandler(HttpClientErrorException.class)
    protected ResponseEntity<Object> handleTooManyRequestsException(
            HttpServletRequest request,
            HttpClientErrorException e
    ) {
        return errorDetailsResponseEntity(e, HttpStatus.TOO_MANY_REQUESTS,
                String.format(ErrorConstants.TOO_MANY_REQUESTS.getErrorMessage(), resendInterval));
    }

    @ExceptionHandler(IdamServiceException.class)
    protected ResponseEntity<Object> handleIdamServiceException(
            HttpServletRequest request,
            IdamServiceException e
    ) {
        return errorDetailsResponseEntity(e, e.getHttpStatus(), resolveStatusAndReturnMessage(e.getHttpStatus()));
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

        log.error("{}:: {}", loggingComponentName, LOG_STRING, ex);
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorMessage(errorMsg)
                .errorDescription(getRootException(ex).getLocalizedMessage())
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, httpStatus);
    }

    private ResponseEntity<Object> patternErrorDetailsResponseEntity(Exception ex, HttpStatus httpStatus,
                                                                     String errorMsg) {
        String errorDesc;

        try {
            errorDesc = ex.getMessage().substring(ex.getMessage().lastIndexOf("default message"));
            errorDesc = errorDesc.replace("default message [", "").replace("]]",
                    "");
        } catch (IndexOutOfBoundsException e) {
            errorDesc = getRootException(ex).getLocalizedMessage();
        }

        log.error("{}:: {}", loggingComponentName, LOG_STRING, ex);
        ErrorResponse errorDetails = ErrorResponse.builder()
                .errorMessage(errorMsg)
                .errorDescription(errorDesc)
                .timeStamp(getTimeStamp())
                .build();

        return new ResponseEntity<>(
                errorDetails, httpStatus);
    }
}
