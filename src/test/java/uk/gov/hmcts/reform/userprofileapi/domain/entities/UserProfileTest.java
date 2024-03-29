package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;


class UserProfileTest {

    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(CREATED).build());


    @Test
    void test_create_and_get_successfully() {
        UserProfileCreationData data = buildCreateUserProfileData();
        UserProfile userProfile = new UserProfile(data, HttpStatus.CREATED);

        assertThat(userProfile.getId()).isNull();
        assertThat(userProfile.getEmail()).isEqualTo(data.getEmail().toLowerCase());
        assertThat(userProfile.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(userProfile.getLastName()).isEqualTo(data.getLastName());

        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();

        assertThat(userProfile.getUserCategory()).hasToString(data.getUserCategory());
        assertThat(userProfile.getUserType()).hasToString(data.getUserType());

        assertThat(userProfile.getStatus()).isEqualTo(IdamStatus.PENDING);
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
    void test_set_defaults_when_optional_field_is_not_provided() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
    }

    @Test
    void test_set_defaults_when_language_pref_field_is_not_provided() {
        UserProfileCreationData data = buildCreateUserProfileData();

        data.setLanguagePreference(null);

        UserProfile userProfile = new UserProfile(data, HttpStatus.CREATED);
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
    }

    @Test
    void test_roles_are_empty_when_roles_field_is_not_provided() {
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfile userProfile = new UserProfile(data, HttpStatus.CREATED);

        List<String> roles = null;
        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(UUID.randomUUID().toString(),
                "test@test.com", "Test", "Test", roles,
                true, true, CREATED, "test status");

        userProfile.setRoles(idamRolesInfo);

        assertThat(userProfile.getRoles()).isNotNull();
        assertThat(userProfile.getRoles()).isEmpty();
    }

    @Test
    void test_roles_are_empty_when_idam_roles_info_is_null() {
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfile userProfile = new UserProfile(data, HttpStatus.CREATED);

        IdamRolesInfo idamRolesInfo = null;

        userProfile.setRoles(idamRolesInfo);

        assertThat(userProfile.getRoles()).isNotNull();
        assertThat(userProfile.getRoles()).isEmpty();
    }

}
