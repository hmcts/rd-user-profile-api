package uk.gov.hmcts.reform.userprofileapi.domain;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.util.List;
import java.util.Optional;

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
    private HttpStatus responseStatusCode;
    private String statusMessage;

    public IdamRolesInfo(Optional<ResponseEntity<IdamUserResponse>> entity, HttpStatus idamResponseStatusCode) {
        if (entity.isPresent() && null != entity.get().getBody()) {
            id = entity.get().getBody().getId();
            roles = entity.get().getBody().getRoles();
            email = entity.get().getBody().getEmail();
            forename = entity.get().getBody().getForename();
            surname = entity.get().getBody().getSurname();
            responseStatusCode = idamResponseStatusCode;
            statusMessage = resolveStatusAndReturnMessage(idamResponseStatusCode);
        }
    }

    public boolean isSuccessFromIdam() {
        return responseStatusCode.is2xxSuccessful();
    }

}