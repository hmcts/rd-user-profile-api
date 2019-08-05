package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.util.CollectionUtils;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Getter
@Setter
@NoArgsConstructor
public class GetUserProfileWithRolesResponse extends GetUserProfileResponse {

    private List<String> roles;
    @JsonProperty
    private Integer idamStatusCode;
    @JsonProperty
    private String idamMessage;

    public GetUserProfileWithRolesResponse(UserProfile userProfile) {
        super(userProfile);
        roles = userProfile.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            idamStatusCode = userProfile.getErrorStatusCode();
            idamMessage = userProfile.getErrorMessage();
        }
    }
}
