package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

import java.time.LocalDateTime;
import java.util.HashSet;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

public class UserProfileMapperTest {

    private UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(CREATED).build());
    private UserProfile userProfile = new UserProfile(userProfileCreationData,
            idamRegistrationInfo.getIdamRegistrationResponse());
    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com",
            "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(),
            new HashSet<RoleName>());

    @Test
    public void test_mapUpdatableFields() {
        UserProfileMapper.mapUpdatableFields(updateUserProfileData, userProfile, false);

        assertThat(userProfile.getEmail()).isEqualTo("email@net.com");
        assertThat(userProfile.getFirstName()).isEqualTo("firstName");
        assertThat(userProfile.getLastName()).isEqualTo("lastName");
        assertThat(userProfile.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    public void test_deriveStatusFlagWhenStatusIsActive() {
        updateUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        assertEquals(true, UserProfileMapper.deriveStatusFlag(updateUserProfileData));
    }

    @Test
    public void test_deriveStatusFlagWhenStatusIsSuspended() {
        updateUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        assertEquals(false, UserProfileMapper.deriveStatusFlag(updateUserProfileData));
    }

    @Test
    public void test_deriveStatusFlagWhenStatusIsNull() {
        updateUserProfileData.setIdamStatus(null);
        assertEquals(false, UserProfileMapper.deriveStatusFlag(updateUserProfileData));
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

    @Test
    public void test_mapUpdatableFieldsForReInvite() {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getFirstName()).thenReturn(userProfileCreationData.getFirstName());
        when(userProfileMock.getLastName()).thenReturn(userProfileCreationData.getLastName());

        UserProfileMapper.mapUpdatableFieldsForReInvite(userProfileCreationData, userProfileMock);

        assertThat(userProfileMock.getFirstName()).isEqualTo(userProfileCreationData.getFirstName());
        assertThat(userProfileMock.getLastName()).isEqualTo(userProfileCreationData.getLastName());
        verify(userProfileMock, times(1)).setLastUpdated(any(LocalDateTime.class));
        verify(userProfileMock, times(1)).getFirstName();
        verify(userProfileMock, times(1)).getLastName();
    }

}