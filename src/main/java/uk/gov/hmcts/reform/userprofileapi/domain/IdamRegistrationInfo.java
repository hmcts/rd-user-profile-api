package uk.gov.hmcts.reform.userprofileapi.domain;

import org.springframework.http.HttpStatus;

public class IdamRegistrationInfo {

    private HttpStatus idamRegistrationResponse;

    public IdamRegistrationInfo(HttpStatus httpStatus) {
        this.idamRegistrationResponse = httpStatus;
    }

    public HttpStatus getIdamRegistrationResponse() {
        return idamRegistrationResponse;
    }
}
