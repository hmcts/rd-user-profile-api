package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Random;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

    private final String idamId = UUID.randomUUID().toString();
    private final String email = "someone@somewhere.com";
    private final String firstname = String.valueOf(new Random().nextInt());
    private final String lastname = String.valueOf(new Random().nextInt());

    @Test
    public void should_create_successfully_with_no_args_constructor() {

        UserProfile userProfile = new UserProfile();
        assertThat(userProfile).isNotNull();
        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getIdamId()).isNull();
        assertThat(userProfile.getEmail()).isNull();
        assertThat(userProfile.getFirstName()).isNull();
        assertThat(userProfile.getLastName()).isNull();

    }

    @Test
    public void should_create_and_get_successfully() {

        UserProfile userProfile = new UserProfile(idamId, email, firstname, lastname);

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getIdamId()).isEqualTo(idamId);
        assertThat(userProfile.getEmail()).isEqualTo(email);
        assertThat(userProfile.getFirstName()).isEqualTo(firstname);
        assertThat(userProfile.getLastName()).isEqualTo(lastname);

    }

    @Test
    public void should_not_allow_null_values() {

        assertThatThrownBy(() -> new UserProfile(null, email, firstname, lastname))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new UserProfile(idamId, null, firstname, lastname))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new UserProfile(idamId, email, null, lastname))
            .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new UserProfile(idamId, email, firstname, null))
            .isInstanceOf(NullPointerException.class);

    }

}
