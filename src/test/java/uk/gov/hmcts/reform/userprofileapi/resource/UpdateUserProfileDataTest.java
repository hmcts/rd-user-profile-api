package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

public class UpdateUserProfileDataTest {

    @Test
    public void test_add_roles_add_when_updated() {
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

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email", "fname",
                "lname", "ACTIVE", new HashSet<RoleName>(), new HashSet<>());

        assertThat(updateUserProfileData.isSameAsUserProfile(userProfile)).isTrue();
    }
}