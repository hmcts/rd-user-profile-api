package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileTest {

    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.CREATED);

    @Test
        public void should_create_successfully_with_no_args_constructor() {

        UserProfile userProfile = new UserProfile();
        assertThat(userProfile).isNotNull();

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getEmail()).isNull();
        assertThat(userProfile.getFirstName()).isNull();
        assertThat(userProfile.getLastName()).isNull();
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);

        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();

        assertThat(userProfile.getUserCategory()).isNull();
        assertThat(userProfile.getUserType()).isNull();

        assertThat(userProfile.getStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(userProfile.getIdamRegistrationResponse()).isNull();


        assertThat(userProfile.getCreated()).isNull();
        assertThat(userProfile.getLastUpdated()).isNull();
        assertThat(userProfile.getResponses()).isEmpty();

    }

    @Test
    public void should_create_and_get_successfully() {

        CreateUserProfileData data = buildCreateUserProfileData();
        UserProfile userProfile = new UserProfile(data, HttpStatus.CREATED);

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getEmail()).isEqualTo(data.getEmail().toLowerCase());
        assertThat(userProfile.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(userProfile.getLastName()).isEqualTo(data.getLastName());

        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();

        assertThat(userProfile.getUserCategory().toString()).isEqualTo(data.getUserCategory());
        assertThat(userProfile.getUserType().toString()).isEqualTo(data.getUserType());

        //assertThat(userProfile.getStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(userProfile.getIdamRegistrationResponse())
            .isEqualTo(idamRegistrationInfo.getIdamRegistrationResponse().value());

        //Timestamps set by hibernate at insertion time
        assertThat(userProfile.getCreated()).isNull();
        assertThat(userProfile.getLastUpdated()).isNull();
        assertThat(userProfile.getResponses()).isEmpty();

        data.setLanguagePreference(null);
        UserProfile userProfile1 = new UserProfile(data, HttpStatus.CREATED);
        assertThat(userProfile1.getLanguagePreference()).isEqualTo(LanguagePreference.EN);

        data.setLanguagePreference("");
        UserProfile userProfile2 = new UserProfile(data, HttpStatus.CREATED);
        assertThat(userProfile2.getLanguagePreference()).isEqualTo(LanguagePreference.EN);

        data.setLanguagePreference(" ");
        UserProfile userProfile3 = new UserProfile(data, HttpStatus.CREATED);
        assertThat(userProfile3.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
    }

    @Test
    public void should_set_defaults_when_optional_field_is_not_provided() {

        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
    }

    @Test
    public void should_set_defaults_when_language_pref_field_is_not_provided() {

        CreateUserProfileData data = buildCreateUserProfileData();
        data.setLanguagePreference(null);
        UserProfile userProfile = new UserProfile(data, HttpStatus.CREATED);
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
    }

}
