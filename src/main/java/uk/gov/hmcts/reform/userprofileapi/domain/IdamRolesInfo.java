package uk.gov.hmcts.reform.userprofileapi.domain;

import static java.util.Objects.nonNull;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getStatusCodeValueFromResponseEntity;
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

    public IdamRolesInfo(ResponseEntity<Object> entity) {
        if (nonNull(entity)  && nonNull(entity.getBody()) && entity.getBody() instanceof IdamUserResponse) {
            IdamUserResponse idamUserResponse = (IdamUserResponse) entity.getBody();
            if (null != idamUserResponse) {
                this.id = idamUserResponse.getId();
                this.roles = idamUserResponse.getRoles();
                this.email = idamUserResponse.getEmail();
                this.forename = idamUserResponse.getForename();
                this.surname = idamUserResponse.getSurname();
                this.active = idamUserResponse.getActive();
                this.pending = idamUserResponse.getPending();
            }

        }
        loadStatusCodes(entity);
    }

    private void loadStatusCodes(ResponseEntity<Object> responseEntity) {
        this.responseStatusCode = HttpStatus.valueOf(getStatusCodeValueFromResponseEntity(responseEntity));
        this.statusMessage = resolveStatusAndReturnMessage(responseEntity);
    }

    public boolean isSuccessFromIdam() {
        return responseStatusCode.is2xxSuccessful();
    }

}