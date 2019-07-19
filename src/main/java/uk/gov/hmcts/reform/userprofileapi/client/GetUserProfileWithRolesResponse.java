package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.annotation.JsonInclude;
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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer idamErrorStatusCode;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String idamErrorMessage;

    public GetUserProfileWithRolesResponse(UserProfile userProfile) {
        super(userProfile);
        roles = userProfile.getRoles();
        if (CollectionUtils.isEmpty(roles)) {
            idamErrorStatusCode = userProfile.getErrorStatusCode();
            idamErrorMessage = userProfile.getErrorMessage();
        }
    }
}
