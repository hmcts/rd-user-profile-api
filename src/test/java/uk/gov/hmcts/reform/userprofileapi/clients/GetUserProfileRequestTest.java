package uk.gov.hmcts.reform.userprofileapi.clients;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfilesRequest;

public class GetUserProfileRequestTest {


    @Test
    public void should_hold_values_after_creation() {

        List<String> userIds = new ArrayList<String>();
        userIds.add(UUID.randomUUID().toString());
        GetUserProfilesRequest getUserProfileRequest = new GetUserProfilesRequest(userIds);


        assertThat(getUserProfileRequest.getUserIds().size()).isEqualTo(1);

    }
}
