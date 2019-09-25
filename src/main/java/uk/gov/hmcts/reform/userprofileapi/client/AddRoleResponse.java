package uk.gov.hmcts.reform.userprofileapi.client;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@NoArgsConstructor
public class AddRoleResponse {

    private String idamStatusCode;
    private String idamMessage;

    public AddRoleResponse(HttpStatus idamStatusCode) {
        loadStatusCodes(idamStatusCode);
    }

    public void loadStatusCodes(HttpStatus idamGetResponseStatusCode) {
        this.idamStatusCode = String.valueOf(idamGetResponseStatusCode.value());
        this.idamMessage = resolveStatusAndReturnMessage(idamGetResponseStatusCode);
    }
}
