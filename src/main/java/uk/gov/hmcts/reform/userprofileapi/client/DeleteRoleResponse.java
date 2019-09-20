package uk.gov.hmcts.reform.userprofileapi.client;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class DeleteRoleResponse  {

    private String roleName;
    private String idamGetResponseStatusCode;
    private String statusMessage;

    public DeleteRoleResponse(String roleName, HttpStatus idamGetResponseStatusCode, String statusMessage) {
        this.roleName = roleName;
        loadStatusCodes(idamGetResponseStatusCode);
    }

    public void loadStatusCodes(HttpStatus idamGetResponseStatusCode) {
        this.idamGetResponseStatusCode = String.valueOf(idamGetResponseStatusCode.value());
        this.statusMessage = resolveStatusAndReturnMessage(idamGetResponseStatusCode);
    }

}
