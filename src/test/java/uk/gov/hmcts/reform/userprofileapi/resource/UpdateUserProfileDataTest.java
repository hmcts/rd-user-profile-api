package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

public class UpdateUserProfileDataTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void should_add_roles_add_when_updated() {
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(new RoleName("pui-case-manager"));
        roles.add(new RoleName("pui-case-organisation"));

        updateUserProfileData.setRolesAdd(roles);
        updateUserProfileData.setRolesDelete(roles);

        assertThat(updateUserProfileData.getRolesAdd().size()).isEqualTo(2);
        assertThat(updateUserProfileData.getRolesDelete().size()).isEqualTo(2);
    }

    @Test
    public void test_isSameAsUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("email");

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email", "fname", "lname", "ACTIVE", new HashSet<RoleName>(), new HashSet<>());

        assertThat(updateUserProfileData.isSameAsUserProfile(userProfile)).isTrue();
    }

    @Test
    public void test_DoesNotCreate_UpdateUserProfileData_WhenFirstName_IsInvalid() {
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email", "<this!is*invalid>", "lname", "ACTIVE", new HashSet<RoleName>(), new HashSet<>());

        Set<ConstraintViolation<UpdateUserProfileData>> violations = validator.validate(updateUserProfileData);
        assertThat(violations.size()).isEqualTo(1);

        updateUserProfileData.setFirstName("%3cscript%3ealert(%22WXSS%22)%3c%2fscript%3e");

        Set<ConstraintViolation<UpdateUserProfileData>> violations1 = validator.validate(updateUserProfileData);
        assertThat(violations1.size()).isEqualTo(1);
    }

    @Test
    public void test_DoesNotCreate_UpdateUserProfileData_WhenLastName_IsInvalid() {
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email", "fname", "<this!is*invalid>", "ACTIVE", new HashSet<RoleName>(), new HashSet<>());

        Set<ConstraintViolation<UpdateUserProfileData>> violations = validator.validate(updateUserProfileData);
        assertThat(violations.size()).isEqualTo(1);

        updateUserProfileData.setLastName("%3cscript%3ealert(%22WXSS%22)%3c%2fscript%3e");

        Set<ConstraintViolation<UpdateUserProfileData>> violations1 = validator.validate(updateUserProfileData);
        assertThat(violations1.size()).isEqualTo(1);
    }
}