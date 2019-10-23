package uk.gov.hmcts.reform.userprofileapi.util;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

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
        UserProfileMapper.mapUpdatableFields(updateUserProfileDataMock, userProfileMock);

        verify(updateUserProfileDataMock, times(1)).isSameAsUserProfile(any());
        verify(userProfileMock, times(1)).setEmail(eq(dummyEmail));
        verify(userProfileMock, times(1)).setFirstName(eq(dummyFirstName));
        verify(userProfileMock, times(1)).setLastName(eq(dummyLastName));
    }


}