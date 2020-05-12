package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.util.List;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;

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
    private ResponseEntity responseEntity;

    public IdamRolesInfo(ResponseEntity entity) {
        responseEntity = entity;
        if (entity != null && entity.getBody() != null && entity.getBody() instanceof IdamUserResponse) {
            IdamUserResponse idamUserResponse = (IdamUserResponse) entity.getBody();
            this.id = idamUserResponse.getId();
            this.roles = idamUserResponse.getRoles();
            this.email = idamUserResponse.getEmail();
            this.forename = idamUserResponse.getForename();
            this.surname = idamUserResponse.getSurname();
            this.active = idamUserResponse.getActive();
            this.pending = idamUserResponse.getPending();
        }
        loadStatusCodes(entity.getStatusCode());
    }

    private void loadStatusCodes(HttpStatus idamGetResponseStatusCode) {
        this.responseStatusCode = idamGetResponseStatusCode;
        this.statusMessage = resolveStatusAndReturnMessage(responseEntity);
    }

    public boolean isSuccessFromIdam() {
        return responseStatusCode.is2xxSuccessful();
    }

}