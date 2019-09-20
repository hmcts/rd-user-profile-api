package uk.gov.hmcts.reform.userprofileapi.client;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileRolesResponse {

    private String responseStatusCode;
    private String statusMessage;
    private List<DeleteRoleResponse> deleteResponses;

    public UserProfileRolesResponse(HttpStatus idamGetResponseStatusCode) {
        loadStatusCodes(idamGetResponseStatusCode);
    }

    public void loadStatusCodes(HttpStatus idamGetResponseStatusCode) {
        this.responseStatusCode = String.valueOf(idamGetResponseStatusCode.value());
        this.statusMessage = resolveStatusAndReturnMessage(idamGetResponseStatusCode);
    }
}

