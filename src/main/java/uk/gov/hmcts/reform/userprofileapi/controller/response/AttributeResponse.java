package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getStatusCodeValueFromResponseEntity;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;


@Getter
@NoArgsConstructor
public class AttributeResponse {

    private Integer idamStatusCode;
    private String idamMessage;

    public AttributeResponse(ResponseEntity<Object> responseEntity) {
        this.idamStatusCode = getStatusCodeValueFromResponseEntity(responseEntity);
        this.idamMessage = resolveStatusAndReturnMessage(responseEntity);

    }

}
