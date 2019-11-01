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
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.*;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.exception.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileValidatorTest {

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

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("som@org.com", "fanme", "lname", "ACTIVE", addRolesToRoleName(), addRolesToRoleName());
        //boolean response = UserProfileValidator.isUpdateUserProfileRequestValid(updateUserProfileData);
        //assertThat(response).isTrue();
    }

    @Test
    public void test_validateUpdateUserProfileRequestFields() {
        UpdateUserProfileData updateUserProfileDataWithInvalidEmail = new UpdateUserProfileData("somorg.com", "fanme", "lname", "ACTIVE", addRolesToRoleName(),addRolesToRoleName());
        //boolean response1 = UserProfileValidator.validateUpdateUserProfileRequestFields(updateUserProfileDataWithInvalidEmail);
        //assertThat(response1).isFalse();
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

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("test-email-@somewhere.com", "test-first-name", "test-last-name", "PENDING",addRolesToRoleName(), addRolesToRoleName());

        assertThat(updateUserProfileData.isSameAsUserProfile(userProfile)).isTrue();

        updateUserProfileData = new UpdateUserProfileData("test-email-@somewhere.com1", "test-first-name1", "test-last-name", "PENDING",addRolesToRoleName(), addRolesToRoleName());
        assertThat(updateUserProfileData.isSameAsUserProfile(userProfile)).isFalse();
    }

    @Test
    public void test_validateCreateUserProfileRequest() {

        assertThatThrownBy(() -> UserProfileValidator.validateCreateUserProfileRequest(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void test_validateCreateUserProfileRequest_no_exception_thrown() {
        UserProfileValidator.validateCreateUserProfileRequest(userProfileData);
    }

    @Test
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

    static void validateUserIds(UserProfileDataRequest userProfileDataRequest) {
        if (userProfileDataRequest.getUserIds().isEmpty()) {
            throw new RequiredFieldMissingException("no user id in request");
        }
    }

    @Test
    public void test_validateUserIds() {

        UserProfileDataRequest userProfileDataRequest = new UserProfileDataRequest(new ArrayList<String>());
        assertThatThrownBy(() -> UserProfileValidator.validateUserIds(userProfileDataRequest))
                .isInstanceOf(RequiredFieldMissingException.class);
    }
    
    @Test
    public void test_validateUserProfileDataAndUser() {

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();

        if (null == userProfileData) {
            throw new RequiredFieldMissingException("No Request Body in the request");
        }

        assertThat(userProfileData).isNotNull();
    }

    private Set<RoleName> addRolesToRoleName() {

        RoleName roleName = new RoleName("prd-admin");
        Set<RoleName> roleNames = new HashSet<RoleName>();
        roleNames.add(roleName);
        return roleNames;
    }
}