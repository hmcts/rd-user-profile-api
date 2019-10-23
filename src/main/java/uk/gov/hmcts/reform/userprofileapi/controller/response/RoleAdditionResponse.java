package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Data
@NoArgsConstructor
public class RoleAdditionResponse {

    private String idamStatusCode;
    private String idamMessage;

    public RoleAdditionResponse(HttpStatus idamStatusCode) {
        this.idamStatusCode = String.valueOf(idamStatusCode.value());
        this.idamMessage = resolveStatusAndReturnMessage(idamStatusCode);
    }
}
