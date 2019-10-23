package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

public class UserProfileResponseTest {

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
        UserProfileResponse userProfileResponse = new UserProfileResponse(userProfile);

        assertThat(userProfileResponse.getIdamId()).isEqualTo(idamId);
        assertThat(userProfileResponse.getEmail()).isEqualTo("a@hmcts.net");
        assertThat(userProfileResponse.getFirstName()).isEqualTo("fname");
        assertThat(userProfileResponse.getLastName()).isEqualTo("lname");
        assertThat(userProfileResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);

        UserProfileResponse userProfileResponse1 = new UserProfileResponse();
        assertThat(userProfileResponse1).isNotNull();


    }
}
