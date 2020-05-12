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

    public IdamRegistrationInfo(ResponseEntity response) {
        this.response = response;
        this.idamRegistrationResponse = response.getStatusCode();
        this.statusMessage = resolveStatusAndReturnMessage(response);
    }

    public boolean isSuccessFromIdam() {
        return idamRegistrationResponse.is2xxSuccessful();
    }

    public boolean isDuplicateUser() {
        return HttpStatus.CONFLICT == idamRegistrationResponse;
    }

}
