package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.mockito.InjectMocks;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileController;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

public class UpdateUserProfileDataTest {

    @InjectMocks
    private UserProfileController sut;

    @Test
    public void should_add_roles_add_when_updated() {
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();
        RoleName roleName1 = new RoleName("pui-case-manager");
        RoleName roleName2 = new RoleName("pui-case-organisation");
        Set<RoleName> roles = new HashSet<RoleName>();
        roles.add(roleName1);
        roles.add(roleName2);
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
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email", "fname", "lname", "ACTIVE", new HashSet<RoleName>(), new HashSet<RoleName>());
        assertThat(updateUserProfileData.isSameAsUserProfile(userProfile)).isTrue();

    }
}