package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

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
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        logException(e);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<String> handleMethodArgumentNotValidException(
        HttpServletRequest request,
        MethodArgumentNotValidException e
    ) {
        logException(e);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<String> handleResourceNotFoundException(
        HttpServletRequest request,
        ResourceNotFoundException e
    ) {
        logException(e);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<String> handleDataIntegrityViolationException(
        HttpServletRequest request,
        DataIntegrityViolationException e
    ) {
        logException(e);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    protected ResponseEntity<String> handleHttpMessageConversionException(
        HttpServletRequest request,
        HttpMessageConversionException e
    ) {
        logException(e);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IdamServiceException.class)
    protected ResponseEntity<String> handleIdamServiceException(
            HttpServletRequest request,
            IdamServiceException e
    ) {
        logException(e);
        return new ResponseEntity<>(e.getHttpStatus());
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<String> handleUnknownException(
        HttpServletRequest request,
        Exception e
    ) {
        logException(e);
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private void logException(Exception e) {
        log.info(LOG_STRING, e.getMessage());
    }
}
