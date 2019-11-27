package uk.gov.hmcts.reform.userprofileapi.exception;

public class RequiredFieldMissingException extends RuntimeException {
    public RequiredFieldMissingException(String message) {
        super(message);
    }
}
