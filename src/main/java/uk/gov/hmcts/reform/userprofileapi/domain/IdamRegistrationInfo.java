package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IdamRegistrationInfo {

    private HttpStatus idamRegistrationResponse;
    private String statusMessage;

    public IdamRegistrationInfo(HttpStatus httpStatus) {
        this.idamRegistrationResponse = httpStatus;
        this.statusMessage = resolveStatusAndReturnMessage(httpStatus);
    }
}
