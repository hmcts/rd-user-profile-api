package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;


public class UserProfileCreationResponseTest {

    @Test
    public void should_hold_values_after_creation() {


        String idamId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        UserProfileCreationResponse userProfileCreationResponse = new UserProfileCreationResponse(userProfile);

        assertThat(userProfileCreationResponse.getIdamId()).isEqualTo(idamId);
        assertThat(userProfileCreationResponse.getIdamRegistrationResponse()).isEqualTo(201);

        UserProfileCreationResponse userProfileCreationResponse1 = new UserProfileCreationResponse();
        assertThat(userProfileCreationResponse1).isNotNull();


    }
}
