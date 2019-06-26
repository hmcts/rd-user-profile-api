package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class IdamRolesInfo {

    private List<String> roles;
    private HttpStatus idamGetResponseStatusCode;
    private String statusMessage;

    public IdamRolesInfo(List<String> roles, HttpStatus idamGetResponseStatusCode) {
        this.roles = roles;
        this.idamGetResponseStatusCode = idamGetResponseStatusCode;
        this.statusMessage = resolveStatusAndReturnMessage(idamGetResponseStatusCode);
    }
}