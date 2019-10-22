package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

public class UserProfileResponseTest {

    @Test
    public void testUserProfileResponse() {
        final String dummyIdamId = UUID.randomUUID().toString();
        final String dummyEmail = "april.oneil@noreply.com";
        final String dummyFirstName = "APRIL";
        final String dummyLastName = "O'NEIL";

        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        when(userProfileMock.getIdamId()).thenReturn(dummyIdamId);
        when(userProfileMock.getIdamRegistrationResponse()).thenReturn(201);
        when(userProfileMock.getEmail()).thenReturn(dummyEmail);
        when(userProfileMock.getFirstName()).thenReturn(dummyFirstName);
        when(userProfileMock.getLastName()).thenReturn(dummyLastName);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);

        UserProfileResponse userProfileResponse = new UserProfileResponse(userProfileMock);

        assertThat(userProfileResponse).isNotNull();
        assertThat(userProfileResponse.getIdamId()).isEqualTo(dummyIdamId);
        assertThat(userProfileResponse.getEmail()).isEqualTo(dummyEmail);
        assertThat(userProfileResponse.getFirstName()).isEqualTo(dummyFirstName);
        assertThat(userProfileResponse.getLastName()).isEqualTo(dummyLastName);
        assertThat(userProfileResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
    }

    @Test
    public void testUserProfileResponseNoArgConstructor() {
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        assertThat(userProfileResponse.getEmail()).isNull();
    }
}
