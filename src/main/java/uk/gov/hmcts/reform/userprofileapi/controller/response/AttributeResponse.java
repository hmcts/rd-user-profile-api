package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


@Getter
@NoArgsConstructor
public class AttributeResponse {

    private Integer idamStatusCode;
    private String idamMessage;

    public AttributeResponse(HttpStatus httpStatus) {
        this.idamStatusCode = httpStatus.value();
        this.idamMessage = resolveStatusAndReturnMessage(httpStatus);

    }

    public AttributeResponse(ResponseEntity responseEntity) {
        this.idamStatusCode = responseEntity.getStatusCodeValue();
        this.idamMessage = resolveStatusAndReturnMessage(responseEntity);

    }

}
