package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.getIdamRolesJson;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UpdateUserProfileData;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileValidatorTest {

    @Test
    public void test_isUserIdValid() {

        assertThatThrownBy(() -> UserProfileValidator.isUserIdValid("", true))
                .isInstanceOf(ResourceNotFoundException.class);

        boolean response = UserProfileValidator.isUserIdValid("", false);
        assertThat(response).isFalse();

        assertThatThrownBy(() -> UserProfileValidator.isUserIdValid("INVALID", true))
                .isInstanceOf(ResourceNotFoundException.class);

        boolean response1 = UserProfileValidator.isUserIdValid("", false);
        assertThat(response1).isFalse();
    }

    @Test
    public void test_isUpdateUserProfileRequestValid() {

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("som@org.com", "fanme", "lname", "ACTIV");
        boolean response = UserProfileValidator.isUpdateUserProfileRequestValid(updateUserProfileData);
        assertThat(response).isFalse();

    }

    @Test
    public void test_validateUpdateUserProfileRequestFields() {

        UpdateUserProfileData updateUserProfileDataWithNull = null;
        UpdateUserProfileData updateUserProfileDataWithInvalidEmail = new UpdateUserProfileData("somorg.com", "fanme", "lname", "ACTIVE");
        boolean response = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithNull);
        assertThat(response).isFalse();

        boolean response1 = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithInvalidEmail);
        assertThat(response1).isFalse();

    }

    @Test
    public void test_isBlankOrSizeInvalid() {

        boolean response = UserProfileValidator.isBlankOrSizeInvalid("", 5);
        assertThat(response).isTrue();

        boolean response1 = UserProfileValidator.isBlankOrSizeInvalid("lname", 2);
        assertThat(response1).isTrue();
    }

    @Test
    public void test_isSameAsExistingUserProfile() {

        CreateUserProfileData userProfileData =
                new CreateUserProfileData(
                        "test-email-@somewhere.com",
                        "test-first-name",
                        "test-last-name",
                        LanguagePreference.EN.toString(),
                        UserCategory.CITIZEN.toString(),
                        UserType.EXTERNAL.toString(),
                        getIdamRolesJson());
        IdamRegistrationInfo idamInfo = new IdamRegistrationInfo(HttpStatus.CREATED, null);
        UserProfile userProfile = new UserProfile(userProfileData, idamInfo.getIdamRegistrationResponse());

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("test-email-@somewhere.com", "test-first-name", "test-last-name", "PENDING");

        boolean response = UserProfileValidator.isSameAsExistingUserProfile(updateUserProfileData, userProfile);
        assertThat(response).isTrue();

        updateUserProfileData = new UpdateUserProfileData("test-l-@somewhere.com", "test-first-name", "test-last-name", "PENDING");
        boolean response1 = UserProfileValidator.isSameAsExistingUserProfile(updateUserProfileData, userProfile);
        assertThat(response1).isFalse();
    }
}
