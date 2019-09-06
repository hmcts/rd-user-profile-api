package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;


public class CreateUserProfileResponseTest {

    @Test
    public void should_hold_values_after_creation() {


        String idamId = UUID.randomUUID().toString();
        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        CreateUserProfileResponse createUserProfileResponse = new CreateUserProfileResponse(userProfile);

        assertThat(createUserProfileResponse.getIdamId()).isEqualTo(idamId);
        assertThat(createUserProfileResponse.getIdamRegistrationResponse()).isEqualTo(201);

        CreateUserProfileResponse createUserProfileResponse1 = new CreateUserProfileResponse();
        assertThat(createUserProfileResponse1).isNotNull();


    }
}
