package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Data
@NoArgsConstructor
public class UserProfileResponse {

    @JsonProperty ("userIdentifier")
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private String idamStatus;
    private RoleAdditionResponse roleAdditionResponse;
    private List<RoleDeletionResponse> roleDeletionResponse;
    private AttributeResponse attributeResponse;
    private List<String> roles;

    public UserProfileResponse(UserProfile userProfile) {
        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.idamStatus = null != userProfile.getStatus() ? userProfile.getStatus().name() : "";
    }
}

