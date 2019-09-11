package uk.gov.hmcts.reform.userprofileapi.client;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileController;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.getIdamRolesJson;

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
        assertThat(updateUserProfileData.getRolesAdd().size()).isEqualTo(2);
    }
}