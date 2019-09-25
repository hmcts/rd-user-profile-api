package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

public class GetUserProfileResponseTest {

    @Test
    public void should_hold_values_after_creation() {


        String idamId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.ACTIVE);
        GetUserProfileResponse getUserProfileResponse = new GetUserProfileResponse(userProfile);

        assertThat(getUserProfileResponse.getIdamId()).isEqualTo(idamId);
        assertThat(getUserProfileResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(getUserProfileResponse.getFirstName()).isEqualTo("fname");
        assertThat(getUserProfileResponse.getLastName()).isEqualTo("lname");
        assertThat(getUserProfileResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);

        GetUserProfileResponse getUserProfileResponse1 = new GetUserProfileResponse();
        assertThat(getUserProfileResponse1).isNotNull();


    }
}
