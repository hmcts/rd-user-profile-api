package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@Getter
public class GetUserProfileWithRolesResponse extends GetUserProfileResponse {

    private List<String> roles;
    @JsonProperty
    private String idamStatusCode;
    @JsonProperty
    private String idamMessage;

    public GetUserProfileWithRolesResponse(UserProfile userProfile, boolean rolesRequired) {
        super(userProfile);
        if (rolesRequired) {
            if (IdamStatus.ACTIVE == userProfile.getStatus()) {
                roles = userProfile.getRoles().isEmpty() ? null : userProfile.getRoles();
            }
            idamStatusCode = userProfile.getErrorStatusCode();
            idamMessage = userProfile.getErrorMessage();
        } else {
            idamStatusCode = " ";
            idamMessage = IdamStatusResolver.NO_IDAM_CALL;
        }
    }
}
