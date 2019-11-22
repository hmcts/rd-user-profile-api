package uk.gov.hmcts.reform.userprofileapi.exception;

import org.springframework.http.HttpStatus;

public class IdamServiceException extends RuntimeException {

    private final HttpStatus httpStatus;

    public IdamServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
