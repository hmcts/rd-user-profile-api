package uk.gov.hmcts.reform.userprofileapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;

import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@Getter
@Setter
@NoArgsConstructor
public class UserProfileWithRolesResponse extends UserProfileResponse {

    private List<String> roles;

    @JsonProperty
    private String idamStatusCode;

    @JsonProperty
    private String idamMessage;

    public UserProfileWithRolesResponse(UserProfile userProfile, boolean rolesRequired) {
        super(userProfile);//TODO remove inheritance
        idamStatusCode = " ";
        idamMessage = IdamStatusResolver.NO_IDAM_CALL;
        if (rolesRequired) {
            if (IdamStatus.ACTIVE == userProfile.getStatus() && userProfile.getRoles().size() > 0) {
                roles = userProfile.getRoles();
            }
            idamStatusCode = userProfile.getErrorStatusCode();
            idamMessage = userProfile.getErrorMessage();
        }
    }
}
