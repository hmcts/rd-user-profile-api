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
                LanguagePreference.EN.toString(),
                UserCategory.CITIZEN.toString(),
                UserType.EXTERNAL.toString(),
                getIdamRolesJson());

        assertThat(userProfileData.getEmail()).isEqualTo("test-email-@somewhere.com");
        assertThat(userProfileData.getFirstName()).isEqualTo("test-first-name");
        assertThat(userProfileData.getLastName()).isEqualTo("test-last-name");
        assertThat(userProfileData.getUserCategory()).isEqualTo(UserCategory.CITIZEN.toString());
        assertThat(userProfileData.getUserType()).isEqualTo(UserType.EXTERNAL.toString());
        assertThat(userProfileData.getRoles()).isEqualTo(getIdamRolesJson());

    }

    @Test
    public void should_throw_exception_when_mandatory_fields_are_null() {

        assertThatThrownBy(() -> new CreateUserProfileData(
            null,
            "test-first-name",
            "test-last-name",
            LanguagePreference.EN.toString(),
            UserCategory.CITIZEN.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("email");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            null,
            "test-last-name",
            LanguagePreference.EN.toString(),
            UserCategory.CITIZEN.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("firstName");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            "test-first-name",
            null,
                LanguagePreference.EN.toString(),
            UserCategory.CITIZEN.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("lastName");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            "test-first-name",
            "test-last-name",
                LanguagePreference.EN.toString(),
            null,
            UserType.EXTERNAL.toString(),
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("userCategory");

        assertThatThrownBy(() -> new CreateUserProfileData(
            "some-email",
            "test-first-name",
            "test-last-name",
                LanguagePreference.EN.toString(),
            UserCategory.PROFESSIONAL.toString(),
            null,
            getIdamRolesJson()))
            .isInstanceOf(RequiredFieldMissingException.class)
            .hasMessageContaining("userType");

    }
}