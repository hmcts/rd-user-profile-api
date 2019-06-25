package uk.gov.hmcts.reform.userprofileapi.domain;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IdamRegistrationInfo {

    private HttpStatus idamRegistrationResponse;

    public IdamRegistrationInfo(HttpStatus httpStatus) {
        this.idamRegistrationResponse = httpStatus;
    }
}
