package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getStatusCodeValueFromResponseEntity;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;


@Getter
@Setter
@NoArgsConstructor
public class AttributeResponse {

    private Integer idamStatusCode;
    private String idamMessage;

    public AttributeResponse(ResponseEntity<Object> responseEntity) {
        this.idamStatusCode = getStatusCodeValueFromResponseEntity(responseEntity);
        this.idamMessage = resolveStatusAndReturnMessage(responseEntity);

    }

}
