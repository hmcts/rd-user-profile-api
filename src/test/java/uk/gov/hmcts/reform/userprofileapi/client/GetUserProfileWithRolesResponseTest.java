package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

public class GetUserProfileWithRolesResponseTest {

    @Test
    public void should_hold_values_after_creation_when_user_roles_required() {

        String idamId = UUID.randomUUID().toString();

        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setErrorStatusCode("200");
        userProfile.setErrorMessage("error message");

        UserProfileWithRolesResponse getUserProfileWithRolesResponse = new UserProfileWithRolesResponse(userProfile, true);

        assertThat(getUserProfileWithRolesResponse.getIdamId()).isEqualTo(idamId);
        assertThat(getUserProfileWithRolesResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(getUserProfileWithRolesResponse.getFirstName()).isEqualTo("fname");
        assertThat(getUserProfileWithRolesResponse.getLastName()).isEqualTo("lname");
        assertThat(getUserProfileWithRolesResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(getUserProfileWithRolesResponse.getRoles()).isNull();
        assertThat(getUserProfileWithRolesResponse.getIdamMessage()).isEqualTo("error message");
        assertThat(getUserProfileWithRolesResponse.getIdamStatusCode()).isEqualTo("200");

        UserProfileResponse userProfileResponse1 = new UserProfileResponse();
        assertThat(userProfileResponse1).isNotNull();
    }

    @Test
    public void should_hold_values_after_creation_when_user_roles_not_required() {


        String idamId = UUID.randomUUID().toString();

        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.ACTIVE);

        UserProfileWithRolesResponse getUserProfileWithRolesResponse = new UserProfileWithRolesResponse(userProfile, false);

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


        String idamId = UUID.randomUUID().toString();

        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.PENDING);
        userProfile.setErrorStatusCode("200");
        userProfile.setErrorMessage("error message");

        UserProfileWithRolesResponse getUserProfileWithRolesResponse = new UserProfileWithRolesResponse(userProfile, true);

        assertThat(getUserProfileWithRolesResponse.getIdamId()).isEqualTo(idamId);
        assertThat(getUserProfileWithRolesResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(getUserProfileWithRolesResponse.getFirstName()).isEqualTo("fname");
        assertThat(getUserProfileWithRolesResponse.getLastName()).isEqualTo("lname");
        assertThat(getUserProfileWithRolesResponse.getIdamStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(getUserProfileWithRolesResponse.getRoles()).isNull();
        assertThat(getUserProfileWithRolesResponse.getIdamMessage()).isEqualTo("error message");
        assertThat(getUserProfileWithRolesResponse.getIdamStatusCode()).isEqualTo("200");

        UserProfileResponse userProfileResponse1 = new UserProfileResponse();
        assertThat(userProfileResponse1).isNotNull();
    }


}
