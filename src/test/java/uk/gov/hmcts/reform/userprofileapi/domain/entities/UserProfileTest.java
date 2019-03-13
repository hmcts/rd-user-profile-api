package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Random;
import java.util.UUID;
import org.agileware.test.PropertiesTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

    private PropertiesTester tester = new PropertiesTester();

    private final String idamId = UUID.randomUUID().toString();
    private final String email = "someone@somewhere.com";
    private final String firstname = String.valueOf(new Random().nextInt());
    private final String lastname = String.valueOf(new Random().nextInt());

    @Test
    public void should_create_successfully() {

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
