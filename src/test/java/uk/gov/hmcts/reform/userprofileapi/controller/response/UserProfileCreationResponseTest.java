package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;


public class UserProfileCreationResponseTest {

    @Test
    public void should_hold_values_after_creation() {
        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        final String idamId = UUID.randomUUID().toString();
        final int dummyIdamRegistrationResponse = 201;

        when(userProfileMock.getIdamId()).thenReturn(idamId);
        when(userProfileMock.getIdamRegistrationResponse()).thenReturn(dummyIdamRegistrationResponse);

        UserProfileCreationResponse sut = new UserProfileCreationResponse(userProfileMock);

        assertThat(sut.getIdamId()).isEqualTo(idamId);
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(201);
    }

    @Test
    public void testUserProfileCreationResponseNoArg() {
        UserProfileCreationResponse sut = new UserProfileCreationResponse();
        assertThat(sut).isNotNull();
    }
}
