package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Random;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileResourceTest {

    private final UUID uuid = UUID.randomUUID();
    private final String idamId = UUID.randomUUID().toString();
    private final String email = "someone@somewhere.com";
    private final String firstname = String.valueOf(new Random().nextInt());
    private final String lastname = String.valueOf(new Random().nextInt());

    @Test
    public void should_create_successfully_with_no_args_constructor() {

        UserProfileResource userProfile = new UserProfileResource();
        assertThat(userProfile).isNotNull();
        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getIdamId()).isNull();
        assertThat(userProfile.getEmail()).isNull();
        assertThat(userProfile.getFirstName()).isNull();
        assertThat(userProfile.getLastName()).isNull();

    }

    @Test
    public void should_create_and_get_successfully() {

        UserProfileResource userProfile = new UserProfileResource(uuid, idamId, email, firstname, lastname);

        assertThat(userProfile.getId()).isEqualTo(uuid);
        assertThat(userProfile.getIdamId()).isEqualTo(idamId);
        assertThat(userProfile.getEmail()).isEqualTo(email);
        assertThat(userProfile.getFirstName()).isEqualTo(firstname);
        assertThat(userProfile.getLastName()).isEqualTo(lastname);

    }

    @Test
    public void should_create_from_user_profile_successfully() {

        UserProfile userProfile = mock(UserProfile.class);

        when(userProfile.getId()).thenReturn(uuid);
        when(userProfile.getIdamId()).thenReturn(idamId);
        when(userProfile.getEmail()).thenReturn(email);
        when(userProfile.getFirstName()).thenReturn(firstname);
        when(userProfile.getLastName()).thenReturn(lastname);

        assertThat(userProfile.getId()).isNotNull();
        assertThat(userProfile.getIdamId()).isEqualTo(idamId);
        assertThat(userProfile.getEmail()).isEqualTo(email);
        assertThat(userProfile.getFirstName()).isEqualTo(firstname);
        assertThat(userProfile.getLastName()).isEqualTo(lastname);

    }

}
