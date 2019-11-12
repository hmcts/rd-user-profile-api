package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
public class AttributeResponse {
    private Integer idamStatusCode;
    private String idamMessage;

    public AttributeResponse(HttpStatus idamStatusCode) {
        this.idamStatusCode = idamStatusCode.value();
        this.idamMessage = resolveStatusAndReturnMessage(idamStatusCode);

    }

}
