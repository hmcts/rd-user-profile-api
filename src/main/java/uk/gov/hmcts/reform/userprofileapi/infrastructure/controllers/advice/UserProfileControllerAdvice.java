package uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers.advice;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;

@ControllerAdvice(basePackages = "uk.gov.hmcts.reform.userprofileapi.infrastructure.controllers")
@RequestMapping(produces = APPLICATION_JSON_VALUE, consumes = APPLICATION_JSON_VALUE)
public class UserProfileControllerAdvice {

    private static final Logger LOG = LoggerFactory.getLogger(UserProfileControllerAdvice.class);

    private static final String logMessage = "handling exception: {}";

    @ExceptionHandler(RequiredFieldMissingException.class)
    protected ResponseEntity<String> handleRequiredFieldMissingException(
        HttpServletRequest request,
        RequiredFieldMissingException e
    ) {
        LOG.info(logMessage, e.getMessage());
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<String> handleResourceNotFoundException(
        HttpServletRequest request,
        ResourceNotFoundException e
    ) {
        LOG.info(logMessage, e.getMessage());
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(RuntimeException.class)
    protected ResponseEntity<String> handleUnknownRuntimeException(
        HttpServletRequest request,
        RuntimeException e
    ) {
        LOG.info(logMessage, e.getMessage());
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
