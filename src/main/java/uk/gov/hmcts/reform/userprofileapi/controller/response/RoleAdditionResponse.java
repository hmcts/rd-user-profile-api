package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getStatusCodeValueFromResponseEntity;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

@Data
@NoArgsConstructor
public class RoleAdditionResponse {

    private String idamStatusCode;
    private String idamMessage;

    public RoleAdditionResponse(ResponseEntity<Object> responseEntity) {
        this.idamStatusCode = String.valueOf(getStatusCodeValueFromResponseEntity(responseEntity));
        this.idamMessage = resolveStatusAndReturnMessage(responseEntity);
    }
}
