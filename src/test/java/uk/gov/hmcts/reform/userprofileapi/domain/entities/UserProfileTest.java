package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileDataMandatoryFieldsOnly;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.CreationChannel;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserProfileStatus;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);

    @Test
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

        assertThat(userProfile.getIdamStatus()).isNull();
        assertThat(userProfile.getIdamRegistrationResponse()).isNull();


        assertThat(userProfile.getCreatedTs()).isNull();
        assertThat(userProfile.getLastUpdatedTs()).isNull();

    }

    @Test
    public void should_create_and_get_successfully() {

        CreateUserProfileData data = buildCreateUserProfileData();
        UserProfile userProfile = new UserProfile(data,
            new IdamRegistrationInfo(HttpStatus.ACCEPTED));

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getEmail()).isEqualTo(data.getEmail());
        assertThat(userProfile.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(userProfile.getLastName()).isEqualTo(data.getLastName());

        assertThat(userProfile.getEmailCommsConsentTs())
            .isBetween(LocalDateTime.now().minusSeconds(10), LocalDateTime.now());
        assertThat(userProfile.getPostalCommsConsentTs())
            .isBetween(LocalDateTime.now().minusSeconds(10), LocalDateTime.now());

        assertThat(userProfile.getUserCategory().toString()).isEqualTo(data.getUserCategory());
        assertThat(userProfile.getUserType().toString()).isEqualTo(data.getUserType());

        assertThat(userProfile.getIdamStatus()).isNull();
        assertThat(userProfile.getIdamRegistrationResponse())
            .isEqualTo(idamRegistrationInfo.getIdamRegistrationResponse().value());

        //Timestamps set by hibernate at insertion time
        assertThat(userProfile.getCreatedTs()).isNull();
        assertThat(userProfile.getLastUpdatedTs()).isNull();
    }

    @Test
    public void should_set_defaults_when_optional_field_is_not_provided() {

        UserProfile userProfile = new UserProfile(buildCreateUserProfileDataMandatoryFieldsOnly(),
            new IdamRegistrationInfo(HttpStatus.ACCEPTED)
        );

        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs())
            .isBetween(LocalDateTime.now().minusSeconds(10), LocalDateTime.now());
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getPostalCommsConsentTs())
            .isBetween(LocalDateTime.now().minusSeconds(10), LocalDateTime.now());
    }


}
