package uk.gov.hmcts.reform.userprofileapi.controller.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;


@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class UserProfileWithRolesResponse extends UserProfileResponse {

    private List<String> roles;

    @JsonProperty
    private String idamStatusCode;

    @JsonProperty
    private String idamMessage;

    public UserProfileWithRolesResponse(UserProfile userProfile, boolean rolesRequired) {
        super(userProfile);//tbc remove inheritance
        idamStatusCode = " ";
        idamMessage = IdamStatusResolver.NO_IDAM_CALL;
        if (rolesRequired) {
            if (IdamStatus.ACTIVE == userProfile.getStatus() && !userProfile.getRoles().isEmpty()) {
                roles = userProfile.getRoles();
            }
            idamStatusCode = userProfile.getErrorStatusCode();
            idamMessage = userProfile.getErrorMessage();
        }
    }
}
