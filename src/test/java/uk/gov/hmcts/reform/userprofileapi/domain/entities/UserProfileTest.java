package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CREATED);

    @Test
    @Ignore
    public void should_represent_same_number_of_fields_as_in_db() {
        long numberOfFieldsInDb = 19;
        long fieldCount = Stream.of(UserProfile.class.getDeclaredFields())
            .filter(field -> !field.getName().startsWith("$"))
            .map(Field::getName)
            .count();

        assertThat(fieldCount).isEqualTo(numberOfFieldsInDb);

    }


    @Test
    public void should_create_successfully_with_no_args_constructor() {

        UserProfile userProfile = new UserProfile();
        assertThat(userProfile).isNotNull();

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getEmail()).isNull();
        assertThat(userProfile.getFirstName()).isNull();
        assertThat(userProfile.getLastName()).isNull();
        assertThat(userProfile.getLanguagePreference()).isNull();

        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();

        assertThat(userProfile.getUserCategory()).isNull();
        assertThat(userProfile.getUserType()).isNull();

        assertThat(userProfile.getStatus()).isNull();
        assertThat(userProfile.getIdamRegistrationResponse()).isNull();


        assertThat(userProfile.getCreated()).isNull();
        assertThat(userProfile.getLastUpdated()).isNull();
        assertThat(userProfile.getResponses()).isEmpty();

    }

    @Test
    public void should_create_and_get_successfully() {

        CreateUserProfileData data = buildCreateUserProfileData();
        UserProfile userProfile = new UserProfile(data,
            new IdamRegistrationInfo(HttpStatus.CREATED));

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getEmail()).isEqualTo(data.getEmail().toLowerCase());
        assertThat(userProfile.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(userProfile.getLastName()).isEqualTo(data.getLastName());

        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();

        assertThat(userProfile.getUserCategory().toString()).isEqualTo(data.getUserCategory());
        assertThat(userProfile.getUserType().toString()).isEqualTo(data.getUserType());

        assertThat(userProfile.getStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(userProfile.getIdamRegistrationResponse())
            .isEqualTo(idamRegistrationInfo.getIdamRegistrationResponse().value());

        //Timestamps set by hibernate at insertion time
        assertThat(userProfile.getCreated()).isNull();
        assertThat(userProfile.getLastUpdated()).isNull();
        assertThat(userProfile.getResponses()).isEmpty();
    }

    @Test
    public void should_set_defaults_when_optional_field_is_not_provided() {

        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(),
            new IdamRegistrationInfo(HttpStatus.CREATED)
        );

        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
    }


}
