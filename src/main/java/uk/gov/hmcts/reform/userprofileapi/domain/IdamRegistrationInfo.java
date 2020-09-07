package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getStatusCodeValueFromResponseEntity;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Getter
public class IdamRegistrationInfo {

    private HttpStatus idamRegistrationResponse;
    private String statusMessage;
    private ResponseEntity<Object> response;

    public IdamRegistrationInfo(ResponseEntity<Object> response) {
        this.response = response;
        this.idamRegistrationResponse = HttpStatus.valueOf(getStatusCodeValueFromResponseEntity(response));
        this.statusMessage = resolveStatusAndReturnMessage(response);
    }

    public boolean isSuccessFromIdam() {
        return idamRegistrationResponse.is2xxSuccessful();
    }

    public boolean isDuplicateUser() {
        return HttpStatus.CONFLICT == idamRegistrationResponse;
    }

}
