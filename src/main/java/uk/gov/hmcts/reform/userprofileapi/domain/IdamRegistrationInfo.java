package uk.gov.hmcts.reform.userprofileapi.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getStatusCodeValueFromResponseEntity;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

@Getter
@AllArgsConstructor
public class IdamRegistrationInfo {

    private final HttpStatus idamRegistrationResponse;
    private final String statusMessage;
    private final ResponseEntity<Object> response;

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
