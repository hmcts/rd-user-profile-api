package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.List;

@Data
@NoArgsConstructor
public class UserProfileResponse {

    @JsonProperty ("userIdentifier")
    private String idamId;
    private String email;
    private String firstName;
    private String lastName;
    private IdamStatus idamStatus;
    private RoleAdditionResponse addRolesResponse;
    private List<RoleDeletionResponse> deleteRolesResponse;
    private List<String> roles;

    public UserProfileResponse(UserProfile userProfile) {
        requireNonNull(userProfile, "userProfile must not be null");
        this.idamId = userProfile.getIdamId();
        this.email = userProfile.getEmail();
        this.firstName = userProfile.getFirstName();
        this.lastName = userProfile.getLastName();
        this.idamStatus = userProfile.getStatus();
    }

    public UserProfileResponse(UserProfile userProfile, boolean rolesRequired) {
        this(userProfile);
        //super(userProfile);//TODO remove inheritance
        //damStatusCode = " ";
        //idamMessage = IdamStatusResolver.NO_IDAM_CALL;
        if (rolesRequired) {
            if (IdamStatus.ACTIVE == userProfile.getStatus() && userProfile.getRoles().size() > 0) {
                roles = userProfile.getRoles();
            }
            addRolesResponse.setIdamStatusCode(userProfile.getErrorStatusCode());
            addRolesResponse.setIdamMessage(userProfile.getErrorMessage());
        }
    }
}

