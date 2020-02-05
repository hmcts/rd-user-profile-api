package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

public class UserProfileMapperTest {

    private UserProfileCreationData userProfileCreationData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo.getIdamRegistrationResponse());
    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com", "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(), new HashSet<RoleName>());

    @Test
    public void mapUpdatableFields() {
        UserProfileMapper.mapUpdatableFields(updateUserProfileData, userProfile, false);

        assertThat(userProfile.getEmail()).isEqualTo("email@net.com");
        assertThat(userProfile.getFirstName()).isEqualTo("firstName");
        assertThat(userProfile.getLastName()).isEqualTo("lastName");
        assertThat(userProfile.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    public void test_deriveStatusFlagWhenStatusIsActive() {
        updateUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        assertThat(UserProfileMapper.deriveStatusFlag(updateUserProfileData)).isTrue();
    }

    @Test
    public void test_deriveStatusFlagWhenStatusIsSuspended() {
        updateUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        assertThat(UserProfileMapper.deriveStatusFlag(updateUserProfileData)).isFalse();
    }

    @Test
    public void test_mapIdamUpdateStatusRequestWhenStatusIsActive() {
        updateUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        UpdateUserDetails updateUserDetails = UserProfileMapper.mapIdamUpdateStatusRequest(updateUserProfileData);

        assertThat(updateUserDetails.getForename()).isEqualTo("firstName");
        assertThat(updateUserDetails.getSurname()).isEqualTo("lastName");
        assertThat(updateUserDetails.getActive()).isTrue();
    }

    @Test
    public void test_mapIdamUpdateStatusRequestWhenStatusIsSuspended() {
        updateUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UpdateUserDetails updateUserDetails = UserProfileMapper.mapIdamUpdateStatusRequest(updateUserProfileData);

        assertThat(updateUserDetails.getForename()).isEqualTo("firstName");
        assertThat(updateUserDetails.getSurname()).isEqualTo("lastName");
        assertThat(updateUserDetails.getActive()).isFalse();
    }

}