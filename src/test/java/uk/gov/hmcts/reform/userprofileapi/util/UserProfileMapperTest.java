package uk.gov.hmcts.reform.userprofileapi.util;

import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.ResponseEntity.status;

class UserProfileMapperTest {

    private final UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(CREATED).build());
    private final UserProfile userProfile = new UserProfile(userProfileCreationData,
            idamRegistrationInfo.getIdamRegistrationResponse());
    private final UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("test@test.com",
            "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(),
            new HashSet<RoleName>());

    @Test
    void test_mapUpdatableFields() {
        UserProfileMapper.mapUpdatableFields(updateUserProfileData, userProfile, false);

        assertThat(userProfile.getEmail()).isEqualTo("test@test.com");
        assertThat(userProfile.getFirstName()).isEqualTo("firstName");
        assertThat(userProfile.getLastName()).isEqualTo("lastName");
        assertThat(userProfile.getStatus().name()).isEqualTo("ACTIVE");
    }

    @Test
    void test_deriveStatusFlagWhenStatusIsActive() {
        updateUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        assertTrue(UserProfileMapper.deriveStatusFlag(updateUserProfileData));
    }

    @Test
    void test_deriveStatusFlagWhenStatusIsSuspended() {
        updateUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        assertFalse(UserProfileMapper.deriveStatusFlag(updateUserProfileData));
    }

    @Test
    void test_deriveStatusFlagWhenStatusIsNull() {
        updateUserProfileData.setIdamStatus(null);
        assertFalse(UserProfileMapper.deriveStatusFlag(updateUserProfileData));
    }


    @Test
    void test_mapIdamUpdateStatusRequestWhenStatusIsActive() {
        updateUserProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        UpdateUserDetails updateUserDetails = UserProfileMapper.mapIdamUpdateStatusRequest(updateUserProfileData);

        assertThat(updateUserDetails.getForename()).isEqualTo("firstName");
        assertThat(updateUserDetails.getSurname()).isEqualTo("lastName");
        assertThat(updateUserDetails.getActive()).isTrue();
    }

    @Test
    void test_mapIdamUpdateStatusRequestWhenStatusIsSuspended() {
        updateUserProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UpdateUserDetails updateUserDetails = UserProfileMapper.mapIdamUpdateStatusRequest(updateUserProfileData);

        assertThat(updateUserDetails.getForename()).isEqualTo("firstName");
        assertThat(updateUserDetails.getSurname()).isEqualTo("lastName");
        assertThat(updateUserDetails.getActive()).isFalse();
    }

    @Test
    void test_mapUpdatableFieldsForReInvite() {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getFirstName()).thenReturn("firstName");
        when(userProfileMock.getLastName()).thenReturn("lastName");

        UserProfileMapper.mapUpdatableFieldsForReInvite(userProfileCreationData, userProfileMock);

        assertThat(userProfileMock.getFirstName()).isEqualTo("firstName");
        assertThat(userProfileMock.getLastName()).isEqualTo("lastName");
        verify(userProfileMock, times(1)).setLastUpdated(any(LocalDateTime.class));
        verify(userProfileMock, times(1)).getFirstName();
        verify(userProfileMock, times(1)).getLastName();
    }

}
