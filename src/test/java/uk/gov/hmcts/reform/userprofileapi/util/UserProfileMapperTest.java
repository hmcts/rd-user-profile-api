package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public class UserProfileMapperTest {

    private UpdateUserProfileData updateUserProfileDataMock = Mockito.mock(UpdateUserProfileData.class);

    private UserProfile userProfileMock = Mockito.mock(UserProfile.class);

    private final String dummyEmail = "aon@noreply.com";

    private final String dummyFirstName = "April";

    private final String dummyLastName = "O'Neil";

    @Before
    public void setUp() {
        when(updateUserProfileDataMock.getEmail()).thenReturn(dummyEmail);
        when(updateUserProfileDataMock.getFirstName()).thenReturn(dummyFirstName);
        when(updateUserProfileDataMock.getLastName()).thenReturn(dummyLastName);
    }

    @Test
    public void mapUpdatableFields() {
        UserProfileMapper.mapUpdatableFields(updateUserProfileDataMock, userProfileMock, false);

        verify(updateUserProfileDataMock, times(1)).isSameAsUserProfile(any());
        verify(userProfileMock, times(1)).setEmail(eq(dummyEmail));
        verify(userProfileMock, times(1)).setFirstName(eq(dummyFirstName));
        verify(userProfileMock, times(1)).setLastName(eq(dummyLastName));
    }

    @Test
    public void test_deriveStatusFlag_scenario1() {

        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE.toString());
        assertThat(UserProfileMapper.deriveStatusFlag(updateUserProfileDataMock)).isTrue();

    }

    @Test
    public void test_deriveStatusFlag_scenario2() {

        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.SUSPENDED.toString());
        assertThat(UserProfileMapper.deriveStatusFlag(updateUserProfileDataMock)).isFalse();

    }

    @Test
    public void test_mapIdamUpdateStatusRequest_senario1() {

        when(updateUserProfileDataMock.getFirstName()).thenReturn("fname");
        when(updateUserProfileDataMock.getLastName()).thenReturn("lname");
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.ACTIVE.toString());
        UpdateUserDetails updateUserDetails = UserProfileMapper.mapIdamUpdateStatusRequest(updateUserProfileDataMock);

        assertThat(updateUserDetails.getForename()).isEqualTo("fname");
        assertThat(updateUserDetails.getSurname()).isEqualTo("lname");
        assertThat(updateUserDetails.getActive()).isTrue();

    }

    @Test
    public void test_mapIdamUpdateStatusRequest_senario2() {

        when(updateUserProfileDataMock.getFirstName()).thenReturn("fname");
        when(updateUserProfileDataMock.getLastName()).thenReturn("lname");
        when(updateUserProfileDataMock.getIdamStatus()).thenReturn(IdamStatus.SUSPENDED.toString());
        UpdateUserDetails updateUserDetails = UserProfileMapper.mapIdamUpdateStatusRequest(updateUserProfileDataMock);

        assertThat(updateUserDetails.getForename()).isEqualTo("fname");
        assertThat(updateUserDetails.getSurname()).isEqualTo("lname");
        assertThat(updateUserDetails.getActive()).isFalse();

    }

}