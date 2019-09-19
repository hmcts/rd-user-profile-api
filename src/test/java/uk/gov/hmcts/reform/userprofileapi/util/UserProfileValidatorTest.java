package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.getIdamRolesJson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesRequest;
import uk.gov.hmcts.reform.userprofileapi.client.RoleName;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.*;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileValidatorTest {

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

    @Test
    public void test_isUserIdValid() {

        assertThatThrownBy(() -> UserProfileValidator.isUserIdValid("", true))
                .isInstanceOf(ResourceNotFoundException.class);

        boolean response = UserProfileValidator.isUserIdValid("", false);
        assertThat(response).isFalse();

        boolean response2 = UserProfileValidator.isUserIdValid("INVALID", true);
        assertThat(response2).isTrue();

        boolean response1 = UserProfileValidator.isUserIdValid("", false);
        assertThat(response1).isFalse();
    }

    @Test
    public void test_isUpdateUserProfileRequestValid() {

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("som@org.com", "fanme", "lname", "ACTIVE", addRolesToRoleName());
        boolean response = UserProfileValidator.isUpdateUserProfileRequestValid(updateUserProfileData);
        assertThat(response).isTrue();
    }

    @Test
    public void test_validateUpdateUserProfileRequestFields() {

        UpdateUserProfileData updateUserProfileDataWithNull = null;

        UpdateUserProfileData updateUserProfileDataWithInvalidEmail = new UpdateUserProfileData("somorg.com", "fanme", "lname", "ACTIVE", addRolesToRoleName());
        boolean response = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithNull);
        assertThat(response).isFalse();

        boolean response1 = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithInvalidEmail);
        assertThat(response1).isFalse();
    }

    @Test
    public void test_validateEmailAreValid() {

        String[] validEmails = new String[] {
            "shreedhar.lomte@hmcts.net",
            "shreedhar@yahoo.com",
            "Email.100@yahoo.com",
            "email111@email.com",
            "email.100@email.com.au",
            "email@gmail.com.com",
            "email_231_a@email.com",
            "email_100@yahoo-test.ABC.CoM",
            "email-100@yahoo.com",
            "email-100@email.net",
            "email+100@gmail.com",
            "emAil-100@yahoo-test.com",
            "v.green@ashfords.co.uk",
            "j.robinson@timms-law.com",
            "あいうえお@example.com",
            "emAil@1.com",
            "email@.com.my",
            "email123@gmail.",
            "email123@.com",
            "email123@.com.com",
            ".email@email.com",
            "email()*@gmAil.com",
            "eEmail()*@gmail.com",
            "email@%*.com",
            "email..2002@gmail.com",
            "email.@gmail.com",
            "email@email@gmail.com",
            "email@gmail.com.",
            "email..2002@gmail.com@",
            "-email.23@email.com",
            "$email.3@email.com",
            "!email@email.com",
            "+@Adil61371@gmail.com",
            "_email.23@email.com",
            "email.23@-email.com"};

        for (String email : validEmails) {

            UpdateUserProfileData updateUserProfileDataWithInvalidEmail = new UpdateUserProfileData(email, "fanme", "lname", "ACTIVE", addRolesToRoleName());
            boolean response1 = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithInvalidEmail);
            assertThat(response1).isTrue();
        }
    }

    @Test
    public void test_validateEmailAreInValid() {

        String[] validEmails = new String[]{
            "email.com",
            "email@com",
            "email@",
            "@"
        };

        for (String email : validEmails) {

            UpdateUserProfileData updateUserProfileDataWithInvalidEmail = new UpdateUserProfileData(email, "fanme", "lname", "ACTIVE", addRolesToRoleName());
            boolean response1 = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithInvalidEmail);
            assertThat(response1).isFalse();
        }
    }

    @Test
    public void test_isBlankOrSizeInvalid() {

        boolean response = UserProfileValidator.isBlankOrSizeInvalid("", 5);
        assertThat(response).isTrue();

        boolean response1 = UserProfileValidator.isBlankOrSizeInvalid("lname", 2);
        assertThat(response1).isTrue();

        boolean response3 = UserProfileValidator.isBlankOrSizeInvalid(null, 5);
        assertThat(response3).isTrue();

        boolean response4 = UserProfileValidator.isBlankOrSizeInvalid("lname", 2);
        assertThat(response4).isTrue();

        boolean response5 = UserProfileValidator.isBlankOrSizeInvalid("lname", 8);
        assertThat(response5).isFalse();
    }



    @Test
    public void test_isSameAsExistingUserProfile() {

        IdamRegistrationInfo idamInfo = new IdamRegistrationInfo(HttpStatus.CREATED);
        UserProfile userProfile = new UserProfile(userProfileData, idamInfo.getIdamRegistrationResponse());

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("test-email-@somewhere.com", "test-first-name", "test-last-name", "PENDING",addRolesToRoleName());

        boolean response = UserProfileValidator.isSameAsExistingUserProfile(updateUserProfileData, userProfile);
        assertThat(response).isTrue();

        updateUserProfileData = new UpdateUserProfileData("test-l-@somewhere.com", "test-first-name", "test-last-name", "PENDING",addRolesToRoleName());
        boolean response1 = UserProfileValidator.isSameAsExistingUserProfile(updateUserProfileData, userProfile);
        assertThat(response1).isFalse();
    }

    @Test
    public void test_validateCreateUserProfileRequest() {

        assertThatThrownBy(() -> UserProfileValidator.validateCreateUserProfileRequest(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test(expected = Test.None.class)
    public void test_validateCreateUserProfileRequest_no_exception_thrown() {
        UserProfileValidator.validateCreateUserProfileRequest(userProfileData);
    }

    @Test(expected = Test.None.class)
    public void test_validateCreateUserProfileRequest_no_exception_thrown_when_values_are_null() {
        userProfileData.setUserCategory(null);
        userProfileData.setUserType(null);
        UserProfileValidator.validateCreateUserProfileRequest(userProfileData);
    }

    @Test
    public void test_validateEnumField() {

        assertThatThrownBy(() -> UserProfileValidator.validateEnumField("STATUS", "invalid"))
                .isInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> UserProfileValidator.validateEnumField("LANGUAGEPREFERENCE", "invalid"))
                .isInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> UserProfileValidator.validateEnumField("USERTYPE", "invalid"))
                .isInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> UserProfileValidator.validateEnumField("USERCATEGORY", "invalid"))
                .isInstanceOf(RequiredFieldMissingException.class);
    }

    @Test
    public void test_validateAndReturnBooleanForParam() {

        assertThat(UserProfileValidator.validateAndReturnBooleanForParam("true")).isTrue();
        assertThat(UserProfileValidator.validateAndReturnBooleanForParam("false")).isFalse();

        assertThatThrownBy(() -> UserProfileValidator.validateAndReturnBooleanForParam(null))
                .isInstanceOf(RequiredFieldMissingException.class);

        assertThatThrownBy(() -> UserProfileValidator.validateAndReturnBooleanForParam("invalid"))
                .isInstanceOf(RequiredFieldMissingException.class);
    }

    static void validateUserIds(GetUserProfilesRequest getUserProfilesRequest) {
        if (getUserProfilesRequest.getUserIds().isEmpty()) {
            throw new RequiredFieldMissingException("no user id in request");
        }
    }

    @Test
    public void test_validateUserIds() {

        GetUserProfilesRequest getUserProfilesRequest = new GetUserProfilesRequest(new ArrayList<String>());
        assertThatThrownBy(() -> UserProfileValidator.validateUserIds(getUserProfilesRequest))
                .isInstanceOf(RequiredFieldMissingException.class);
    }
    
    @Test
    public void test_validateUserProfileDataAndUser() {

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();

        if (null == userProfileData) {
            throw new RequiredFieldMissingException("No Request Body in the request");
        }
    }

    private Set<RoleName> addRolesToRoleName() {

        RoleName roleName = new RoleName("prd-admin");
        Set<RoleName> roleNames = new HashSet<RoleName>();
        roleNames.add(roleName);
        return roleNames;
    }
}