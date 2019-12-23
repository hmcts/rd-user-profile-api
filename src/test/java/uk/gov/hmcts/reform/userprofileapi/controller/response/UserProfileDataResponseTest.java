package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileDataResponseTest {

    @Test
    public void testUserProfileDataResponse() {
        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        List<UserProfile> profilesList = new ArrayList<>();
        profilesList.add(userProfileMock);
        profilesList.add(userProfileMock);

        UserProfileDataResponse sut = new UserProfileDataResponse(profilesList, false);

        assertThat(sut.getUserProfiles()).isNotNull();
        assertThat(sut.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    public void testNoArgConstructor() {
        UserProfileDataResponse sut = new UserProfileDataResponse();

        assertThat(sut.getUserProfiles()).isNull();
    }
}
