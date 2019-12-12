package uk.gov.hmcts.reform.userprofileapi.controller.advice;

public class InvalidRequest extends RuntimeException {

    public InvalidRequest(String message) {
        super(message);
    }
}
