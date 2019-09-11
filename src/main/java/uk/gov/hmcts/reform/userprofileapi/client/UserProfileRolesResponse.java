package uk.gov.hmcts.reform.userprofileapi.client;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
public class UserProfileRolesResponse {

    private HttpStatus responseStatusCode;
    private String statusMessage;

    public UserProfileRolesResponse(HttpStatus idamGetResponseStatusCode) {
        loadStatusCodes(idamGetResponseStatusCode);
    }

    private void loadStatusCodes(HttpStatus idamGetResponseStatusCode) {
        this.responseStatusCode = idamGetResponseStatusCode;
        this.statusMessage = resolveStatusAndReturnMessage(idamGetResponseStatusCode);
    }
}

