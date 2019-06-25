package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.http.HttpStatus;

public class IdamServiceException extends RuntimeException {

    private HttpStatus httpStatus;

    public IdamServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }
}
