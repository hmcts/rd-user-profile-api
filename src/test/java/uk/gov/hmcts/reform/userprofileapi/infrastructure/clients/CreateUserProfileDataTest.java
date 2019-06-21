package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.getIdamRolesJson;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;

public class CreateUserProfileDataTest {

    @Test
    public void should_hold_values_after_creation() {

        CreateUserProfileData userProfileData =
            new CreateUserProfileData(
                "test-email-@somewhere.com",
                "test-first-name",
                "test-last-name",
                LanguagePreference.CY.toString(),
                true,
                true,
                UserCategory.CITIZEN.toString(),
                UserType.EXTERNAL.toString(),
                getIdamRolesJson());

        assertThat(userProfileData.getEmail()).isEqualTo("test-email-@somewhere.com");
        assertThat(userProfileData.getFirstName()).isEqualTo("test-first-name");
        assertThat(userProfileData.getLastName()).isEqualTo("test-last-name");
        assertThat(userProfileData.getLanguagePreference()).isEqualTo(LanguagePreference.CY.toString());
        assertThat(userProfileData.isEmailCommsConsent()).isTrue();
        assertThat(userProfileData.isPostalCommsConsent()).isTrue();
        assertThat(userProfileData.getUserCategory()).isEqualTo(UserCategory.CITIZEN.toString());
        assertThat(userProfileData.getUserType()).isEqualTo(UserType.EXTERNAL.toString());
        assertThat(userProfileData.getIdamRoles()).isEqualTo(getIdamRolesJson());

    }

    @Test
    public void should_make_email_lowercase() {

        CreateUserProfileData userProfileData =
                new CreateUserProfileData(
                        "TEST-EMAIL@someWhere.Com",
                        "test-first-name",
                        "test-last-name",
                        LanguagePreference.CY.toString(),
                        true,
                        true,
                        UserCategory.CITIZEN.toString(),
                        UserType.EXTERNAL.toString(),
                        getIdamRolesJson());

        assertThat(userProfileData.getEmail()).isEqualTo("test-email@somewhere.com");
    }

    @Test
    public void should_throw_exception_when_mandatory_fields_are_null() {

        assertThatThrownBy(() -> new CreateUserProfileData(
            null,
            "test-first-name",
            "test-last-name",
            LanguagePreference.CY.toString(),
            true,
            true,
            UserCategory.CITIZEN.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("email");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            null,
            "test-last-name",
            LanguagePreference.CY.toString(),
            true,
            true,
            UserCategory.CITIZEN.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("firstName");
        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            "test-first-name",
            null,
            LanguagePreference.CY.toString(),
            true,
            true,
            UserCategory.CITIZEN.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("lastName");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            "test-first-name",
            "test-last-name",
            LanguagePreference.CY.toString(),
            true,
            true,
            null,
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("userCategory");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            "test-first-name",
            "test-last-name",
            LanguagePreference.CY.toString(),
            true,
            true,
            UserCategory.PROFESSIONAL.toString(),
            null,
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("userType");

    }

    @Test
    public void should_not_throw_exception_when_optional_fields_are_null() {

        assertThat(new CreateUserProfileData(
            "some-email",
            "test-first-name",
            "test-last-name",
            null,
            true,
            true,
            UserCategory.PROFESSIONAL.toString(),
            UserType.INTERNAL.toString(),
            getIdamRolesJson())).isNotNull();

        assertThat(new CreateUserProfileData(
            "some-email",
            "test-first-name",
            "test-last-name",
            LanguagePreference.CY.toString(),
            true,
            true,
            UserCategory.PROFESSIONAL.toString(),
            UserType.INTERNAL.toString(),
            null)).isNotNull();


    }
}