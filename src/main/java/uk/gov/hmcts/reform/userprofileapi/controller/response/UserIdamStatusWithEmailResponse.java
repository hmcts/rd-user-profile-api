package uk.gov.hmcts.reform.userprofileapi.controller.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfileIdamStatus;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class UserIdamStatusWithEmailResponse {
    private List<UserIdamStatusWithEmail> userProfiles;

    public UserIdamStatusWithEmailResponse(List<UserProfileIdamStatus> userProfile) {
        this.userProfiles = userProfile.stream()
                .map(user -> new UserIdamStatusWithEmail(user.getEmail(), user.getStatus()))
                .toList();
    }
}
