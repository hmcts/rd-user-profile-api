package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.client.IdamUserResponse;

@Getter
public class IdamRolesInfo {

    private String id;
    private String email;
    private String forename;
    private String surname;
    private List<String> roles;
    private Boolean active;
    private Boolean pending;
    private HttpStatus responseStatusCode;
    private String statusMessage;

    public IdamRolesInfo(ResponseEntity<IdamUserResponse> entity, HttpStatus idamGetResponseStatusCode) {
        if (entity != null && entity.getBody() != null) {
            this.id = entity.getBody().getId();
            this.roles = entity.getBody().getRoles();
            this.email = entity.getBody().getEmail();
            this.forename = entity.getBody().getForename();
            this.surname = entity.getBody().getSurname();
            this.active = entity.getBody().getActive();
            this.pending = entity.getBody().getPending();
        }
        loadStatusCodes(idamGetResponseStatusCode);
    }

    public IdamRolesInfo(HttpStatus idamGetResponseStatusCode) {
        loadStatusCodes(idamGetResponseStatusCode);
    }

    private void loadStatusCodes(HttpStatus idamGetResponseStatusCode) {
        this.responseStatusCode = idamGetResponseStatusCode;
        this.statusMessage = resolveStatusAndReturnMessage(idamGetResponseStatusCode);
    }

    public boolean isSuccessFromIdam() {
        return responseStatusCode.is2xxSuccessful();
    }

}