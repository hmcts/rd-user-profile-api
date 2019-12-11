package uk.gov.hmcts.reform.userprofileapi.exception;

public class ErrorPersistingException extends RuntimeException {

    public ErrorPersistingException(String message) {
        super(message);
    }
}
