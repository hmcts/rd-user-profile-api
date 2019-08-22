package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

public class GetUserProfilesResponseTest {

    @Test
    public void should_hold_values_after_creation() {

        UserProfile up1 = mock(UserProfile.class);
        UUID idamId = UUID.randomUUID();
        UserProfile userProfile = new UserProfile();
        userProfile.setIdamId(idamId);
        userProfile.setIdamRegistrationResponse(201);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("a@hmcts.net");
        userProfile.setStatus(IdamStatus.ACTIVE);

        UserProfile userProfile1 = new UserProfile();
        userProfile1.setIdamId(idamId);
        userProfile1.setIdamRegistrationResponse(201);
        userProfile1.setFirstName("fname");
        userProfile1.setLastName("lname");
        userProfile1.setEmail("a@hmcts.net");
        userProfile1.setStatus(IdamStatus.ACTIVE);

        List<UserProfile> profilesList = new ArrayList<>();
        profilesList.add(userProfile);
        profilesList.add(userProfile1);

        GetUserProfilesResponse getUserProfilesResponse = new GetUserProfilesResponse(profilesList, false);

        assertThat(getUserProfilesResponse.getUserProfiles()).isNotNull();
    }
}
