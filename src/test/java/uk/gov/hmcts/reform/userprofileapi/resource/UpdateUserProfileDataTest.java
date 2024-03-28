package uk.gov.hmcts.reform.userprofileapi.resource;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateUserProfileDataTest {

    @Test
    void test_add_roles_add_when_updated() {
        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(new RoleName("pui-case-manager"));
        roles.add(new RoleName("pui-case-organisation"));

        updateUserProfileData.setRolesAdd(roles);
        updateUserProfileData.setRolesDelete(roles);

        assertThat(updateUserProfileData.getRolesAdd()).hasSize(2);
        assertThat(updateUserProfileData.getRolesDelete()).hasSize(2);
    }

    @Test
    void test_isSameAsUserProfile() {
        UserProfile userProfile = new UserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setFirstName("fname");
        userProfile.setLastName("lname");
        userProfile.setEmail("email");

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData(UUID.randomUUID().toString(),"email", "fname",
                "lname", "ACTIVE", new HashSet<>(), new HashSet<>());

        assertThat(updateUserProfileData.isSameAsUserProfile(userProfile)).isTrue();
    }
}
