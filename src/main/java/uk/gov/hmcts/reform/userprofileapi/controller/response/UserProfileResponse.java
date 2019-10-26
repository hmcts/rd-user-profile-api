package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
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
    protected RoleAdditionResponse addRolesResponse;
    protected List<RoleDeletionResponse> deleteRolesResponse;
    protected List<String> roles;

    public UserProfileResponse(UserProfile userProfile) {
        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        //TODO det if this check is required and assign IdamStatus.BLANK if it indeed is
        this.idamStatus = null != userProfile.getStatus() ? userProfile.getStatus().name() : "";
        addRolesResponse = new RoleAdditionResponse();
        deleteRolesResponse = new ArrayList<>();
    }

    public UserProfileResponse(UserProfile userProfile, boolean rolesRequired) {
        this(userProfile);
        if (rolesRequired) {
            if (/*IdamStatus.ACTIVE == userProfile.getStatus() && */userProfile.getRoles().size() > 0) {
                roles = userProfile.getRoles();
            }
            addRolesResponse.setIdamStatusCode(userProfile.getErrorStatusCode());
            addRolesResponse.setIdamMessage(userProfile.getErrorMessage());
        }
    }
}

