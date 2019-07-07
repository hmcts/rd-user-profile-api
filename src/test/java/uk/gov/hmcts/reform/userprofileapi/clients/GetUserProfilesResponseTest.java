package uk.gov.hmcts.reform.userprofileapi.clients;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfilesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class GetUserProfilesResponseTest {

    @Test
    public void should_hold_values_after_creation() {


        GetUserProfilesResponse getUserProfilesResponse = new GetUserProfilesResponse(new ArrayList<UserProfile>());

        assertThat(getUserProfilesResponse.getUserProfiles()).isNotNull();
    }
}
