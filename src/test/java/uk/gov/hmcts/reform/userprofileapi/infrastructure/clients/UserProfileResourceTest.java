package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileResourceTest {

    @Test
    public void should_represent_same_number_of_fields_as_in_db() {
        long numberOfFieldsInDb = 19;
        long fieldCount = Stream.of(UserProfileResource.class.getDeclaredFields())
            .filter(field -> !field.getName().startsWith("$"))
            .map(Field::getName)
            .count();

        assertThat(fieldCount).isEqualTo(numberOfFieldsInDb);

    }

    @Test
    public void should_create_successfully_with_no_args_constructor() {

        UserProfileResource userProfile = new UserProfileResource();
        Arrays.asList(userProfile.getClass().getDeclaredMethods()).forEach(method -> {
            try {
                if (method.getName().startsWith("get")) {
                    assertThat(method.invoke(userProfile)).isNull();
                } else if (method.getName().startsWith("is")) {
                    assertThat((Boolean) method.invoke(userProfile)).isFalse();
                }
            } catch (Exception e) {
                Assertions.fail("unable to invoke method %s", method.getName());
            }
        });
    }

    @Test
    public void should_create_from_user_profile_successfully() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfileWithAllFields();
        UserProfileResource userProfileResource = new UserProfileResource(userProfile);

      /*  assertThat(userProfileResource.getId()).isNotNull();
        assertThat(userProfileResource.getId()).isEqualTo(userProfile.getId());
        assertThat(userProfileResource.getEmail()).isEqualTo(userProfile.getEmail());
        assertThat(userProfileResource.getFirstName()).isEqualTo(userProfile.getFirstName());
        assertThat(userProfileResource.getLastName()).isEqualTo(userProfile.getLastName());
        assertThat(userProfileResource.getLanguagePreference()).isEqualTo(userProfile.getLanguagePreference().toString());

        assertThat(userProfileResource.isEmailCommsConsent()).isEqualTo(userProfile.isEmailCommsConsent());
        assertThat(userProfileResource.getEmailCommsConsentTs()).isEqualTo(userProfile.getEmailCommsConsentTs());
        assertThat(userProfileResource.isPostalCommsConsent()).isEqualTo(userProfile.isPostalCommsConsent());
        assertThat(userProfileResource.getPostalCommsConsentTs()).isEqualTo(userProfile.getPostalCommsConsentTs());

        assertThat(userProfileResource.getCreationChannel()).isEqualTo(userProfile.getCreationChannel().toString());
        assertThat(userProfileResource.getUserCategory()).isEqualTo(userProfile.getUserCategory().toString());
        assertThat(userProfileResource.getUserType()).isEqualTo(userProfile.getUserType().toString());

        assertThat(userProfileResource.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(userProfileResource.getIdamStatus()).isEqualTo(userProfile.getIdamStatus());
        assertThat(userProfileResource.getIdamRoles()).isEqualTo(userProfile.getIdamRoles());
        assertThat(userProfileResource.getIdamRegistrationResponse()).isEqualTo(userProfile.getIdamRegistrationResponse());

        assertThat(userProfileResource.getCreatedTs()).isEqualTo(userProfile.getCreatedTs());
        assertThat(userProfileResource.getLastUpdatedTs()).isEqualTo(userProfile.getLastUpdatedTs());
        assertThat(userProfileResource.getIdamRoles()).isEqualTo(userProfile.getIdamRoles());*/

    }

}
