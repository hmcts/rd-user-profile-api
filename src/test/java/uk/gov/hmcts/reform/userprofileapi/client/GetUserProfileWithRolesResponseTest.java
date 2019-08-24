package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

public class GetUserProfileWithRolesResponseTest {

    @Test
    public void should_hold_values_after_creation_when_user_roles_required() {


        UUID idamId = UUID.randomUUID();

        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setErrorStatusCode("200");
        userProfile.setErrorMessage("error message");

        GetUserProfileWithRolesResponse getUserProfileWithRolesResponse = new GetUserProfileWithRolesResponse(userProfile, true);

        assertThat(getUserProfileWithRolesResponse.getIdamId()).isEqualTo(idamId);
        assertThat(getUserProfileWithRolesResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(getUserProfileWithRolesResponse.getFirstName()).isEqualTo("fname");
        assertThat(getUserProfileWithRolesResponse.getLastName()).isEqualTo("lname");
        assertThat(getUserProfileWithRolesResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(getUserProfileWithRolesResponse.getRoles()).isNull();
        assertThat(getUserProfileWithRolesResponse.getIdamMessage()).isEqualTo("error message");
        assertThat(getUserProfileWithRolesResponse.getIdamStatusCode()).isEqualTo("200");

        GetUserProfileResponse getUserProfileResponse1 = new GetUserProfileResponse();
        assertThat(getUserProfileResponse1).isNotNull();
    }

    @Test
    public void should_hold_values_after_creation_when_user_roles_not_required() {


        UUID idamId = UUID.randomUUID();

        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.ACTIVE);

        GetUserProfileWithRolesResponse getUserProfileWithRolesResponse = new GetUserProfileWithRolesResponse(userProfile, false);

        assertThat(getUserProfileWithRolesResponse.getIdamId()).isEqualTo(idamId);
        assertThat(getUserProfileWithRolesResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(getUserProfileWithRolesResponse.getFirstName()).isEqualTo("fname");
        assertThat(getUserProfileWithRolesResponse.getLastName()).isEqualTo("lname");
        assertThat(getUserProfileWithRolesResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(getUserProfileWithRolesResponse.getRoles()).isNull();
        assertThat(getUserProfileWithRolesResponse.getIdamStatusCode()).isEqualTo(" ");
        assertThat(getUserProfileWithRolesResponse.getIdamMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
    }

    @Test
    public void should_hold_values_after_creation_when_user_roles_required_and_status_pending() {


        UUID idamId = UUID.randomUUID();

        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.PENDING);
        userProfile.setErrorStatusCode("200");
        userProfile.setErrorMessage("error message");

        GetUserProfileWithRolesResponse getUserProfileWithRolesResponse = new GetUserProfileWithRolesResponse(userProfile, true);

        assertThat(getUserProfileWithRolesResponse.getIdamId()).isEqualTo(idamId);
        assertThat(getUserProfileWithRolesResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(getUserProfileWithRolesResponse.getFirstName()).isEqualTo("fname");
        assertThat(getUserProfileWithRolesResponse.getLastName()).isEqualTo("lname");
        assertThat(getUserProfileWithRolesResponse.getIdamStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(getUserProfileWithRolesResponse.getRoles()).isNull();
        assertThat(getUserProfileWithRolesResponse.getIdamMessage()).isEqualTo("error message");
        assertThat(getUserProfileWithRolesResponse.getIdamStatusCode()).isEqualTo("200");

        GetUserProfileResponse getUserProfileResponse1 = new GetUserProfileResponse();
        assertThat(getUserProfileResponse1).isNotNull();
    }


}
