package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@NoArgsConstructor
public class RoleDeletionResponse {
    private String roleName;
    private String idamStatusCode;
    private String idamMessage;

    public RoleDeletionResponse(String roleName, ResponseEntity responseEntity) {
        this.roleName = roleName;
        loadStatusCodes(responseEntity);
    }

    private void loadStatusCodes(ResponseEntity responseEntity) {
        this.idamStatusCode = String.valueOf(responseEntity.getStatusCodeValue());
        this.idamMessage = resolveStatusAndReturnMessage(responseEntity);
    }

}
