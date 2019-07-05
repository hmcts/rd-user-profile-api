package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class IdamRegistrationInfo {

    private HttpStatus idamRegistrationResponse;
    private String statusMessage;
    private ResponseEntity response;

    public IdamRegistrationInfo(HttpStatus httpStatus, ResponseEntity response) {
        this.idamRegistrationResponse = httpStatus;
        this.statusMessage = resolveStatusAndReturnMessage(httpStatus);
        this.response = response;
    }

    public boolean isSuccessFromIdam() {
        return idamRegistrationResponse.is2xxSuccessful();
    }

    public boolean isDuplicateUser() {
        return HttpStatus.CONFLICT == idamRegistrationResponse;
    }
}
