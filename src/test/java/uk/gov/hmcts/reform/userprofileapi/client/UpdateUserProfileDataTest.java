package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@RunWith(MockitoJUnitRunner.class)
public class UpdateUserProfileDataTest {

    @Mock
    private UserProfile userProfileMock;

    private final String dummyEmail = "april.o.neil@noreply.com";

    private final String dummyFirstName = "April";

    private final String dummyLastName = "O'Neil";

    private final String dummyStatus = "ACTIVE";

    private final Set<RoleName> dummyRolesAdd = new HashSet<>();

    private final Set<RoleName> dummyRolesDelete = new HashSet<>();

    private UpdateUserProfileData sut = new UpdateUserProfileData(
            dummyEmail, dummyFirstName, dummyLastName, dummyStatus, dummyRolesAdd, dummyRolesDelete);

    @Before
    public void setUp() {
        when(userProfileMock.getEmail()).thenReturn(dummyEmail);
        when(userProfileMock.getFirstName()).thenReturn(dummyFirstName);
        when(userProfileMock.getLastName()).thenReturn(dummyLastName);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.valueOf(dummyStatus));
    }

    @Test
    public void testIsSameAsUserProfile() {
        boolean actual = sut.isSameAsUserProfile(userProfileMock);

        assertThat(actual).isTrue();
    }

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
}