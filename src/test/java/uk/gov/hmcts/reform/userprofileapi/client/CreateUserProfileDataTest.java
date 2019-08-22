package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.getIdamRolesJson;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
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
                false,
                false,
                UserCategory.CITIZEN.toString(),
                UserType.EXTERNAL.toString(),
                getIdamRolesJson());

        assertThat(userProfileData.getEmail()).isEqualTo("test-email-@somewhere.com");
        assertThat(userProfileData.getFirstName()).isEqualTo("test-first-name");
        assertThat(userProfileData.getLastName()).isEqualTo("test-last-name");
        assertThat(userProfileData.getUserCategory()).isEqualTo(UserCategory.CITIZEN.toString());
        assertThat(userProfileData.getUserType()).isEqualTo(UserType.EXTERNAL.toString());
        assertThat(userProfileData.getRoles()).isEqualTo(getIdamRolesJson());

        CreateUserProfileData userProfileData1 = new CreateUserProfileData();
        assertThat(userProfileData1).isNotNull();

    }
}