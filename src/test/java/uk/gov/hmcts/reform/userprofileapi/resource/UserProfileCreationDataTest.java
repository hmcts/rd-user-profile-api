package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.getIdamRolesJson;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;

public class UserProfileCreationDataTest {

    @Test
    public void test_hold_values_after_creation() {
        UserProfileCreationData userProfileData =
            new UserProfileCreationData(
                "test-email-@somewhere.com",
                "test-first-name",
                "test-last-name",
                LanguagePreference.EN.toString(),
                false,
                false,
                UserCategory.CITIZEN.toString(),
                UserType.EXTERNAL.toString(),
                getIdamRolesJson(),false);

        assertThat(userProfileData.getEmail()).isEqualTo("test-email-@somewhere.com");
        assertThat(userProfileData.getFirstName()).isEqualTo("test-first-name");
        assertThat(userProfileData.getLastName()).isEqualTo("test-last-name");
        assertThat(userProfileData.getUserCategory()).isEqualTo(UserCategory.CITIZEN.toString());
        assertThat(userProfileData.getLanguagePreference()).isEqualTo(LanguagePreference.EN.toString());
        assertThat(userProfileData.getUserType()).isEqualTo(UserType.EXTERNAL.toString());
        assertThat(userProfileData.getRoles()).isEqualTo(getIdamRolesJson());

        userProfileData.setStatus(IdamStatus.ACTIVE);
        assertThat(userProfileData.getStatus()).isEqualTo(IdamStatus.ACTIVE);

        userProfileData.setStatus(null);
        assertThat(userProfileData.getStatus()).isEqualTo(IdamStatus.ACTIVE);

        UserProfileCreationData userProfileData1 = new UserProfileCreationData();
        assertThat(userProfileData1).isNotNull();
    }
}