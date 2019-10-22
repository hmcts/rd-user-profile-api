package uk.gov.hmcts.reform.userprofileapi;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;


@Getter
@Setter
@NoArgsConstructor
public class AttributeResponse {

    private Integer idamStatusCode;
    private String idamMessage;

    public AttributeResponse(HttpStatus idamStatusCode) {
        this.idamStatusCode = idamStatusCode.value();
        this.idamMessage = resolveStatusAndReturnMessage(idamStatusCode);

    }

}
