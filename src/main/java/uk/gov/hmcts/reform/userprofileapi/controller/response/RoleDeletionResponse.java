package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class RoleDeletionResponse {
    private String roleName;
    private String idamStatusCode;
    private String idamMessage;

    public RoleDeletionResponse(String roleName, HttpStatus idamStatusCode) {
        this.roleName = roleName;
        loadStatusCodes(idamStatusCode);
    }

    public void loadStatusCodes(HttpStatus idamStatusCode) {
        this.idamStatusCode = String.valueOf(idamStatusCode.value());
        this.idamMessage = resolveStatusAndReturnMessage(idamStatusCode);
    }

}
