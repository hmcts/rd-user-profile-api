package uk.gov.hmcts.reform.userprofileapi.clients;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;


@Getter
@Setter
@NoArgsConstructor
public class GetUserProfileWithRolesResponse extends GetUserProfileResponse {

    private List<String> roles;

    public GetUserProfileWithRolesResponse(UserProfile userProfile) {
        super(userProfile);
        roles = userProfile.getRoles();
    }
}
