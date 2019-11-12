package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;


public class UserProfileResponseTest {

    @Test
    public void testUserProfileResponse() {
        final String dummyIdamId = UUID.randomUUID().toString();
        final String dummyEmail = "april.oneil@noreply.com";
        final String dummyFirstName = "APRIL";
        final String dummyLastName = "O'NEIL";
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manager");

        UserProfile userProfileMock = Mockito.mock(UserProfile.class);

        when(userProfileMock.getIdamId()).thenReturn(dummyIdamId);
        when(userProfileMock.getIdamRegistrationResponse()).thenReturn(201);
        when(userProfileMock.getEmail()).thenReturn(dummyEmail);
        when(userProfileMock.getFirstName()).thenReturn(dummyFirstName);
        when(userProfileMock.getLastName()).thenReturn(dummyLastName);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);
        when(userProfileMock.getRoles()).thenReturn(roles);
        when(userProfileMock.getErrorMessage()).thenReturn("someErrorMessage");
        when(userProfileMock.getErrorStatusCode()).thenReturn("200");

        UserProfileResponse userProfileResponse = new UserProfileResponse(userProfileMock);

        assertThat(userProfileResponse).isNotNull();
        assertThat(userProfileResponse.getIdamId()).isEqualTo(dummyIdamId);
        assertThat(userProfileResponse.getEmail()).isEqualTo(dummyEmail);
        assertThat(userProfileResponse.getFirstName()).isEqualTo(dummyFirstName);
        assertThat(userProfileResponse.getLastName()).isEqualTo(dummyLastName);
        assertThat(userProfileResponse.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());

        UserProfileResponse userProfileResponse1 = new UserProfileResponse(userProfileMock);
        assertThat(userProfileResponse1).isNotNull();
        assertThat(userProfileResponse1.getIdamId()).isEqualTo(dummyIdamId);
        assertThat(userProfileResponse1.getEmail()).isEqualTo(dummyEmail);
        assertThat(userProfileResponse1.getFirstName()).isEqualTo(dummyFirstName);
        assertThat(userProfileResponse1.getLastName()).isEqualTo(dummyLastName);
        assertThat(userProfileResponse1.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());

    }

    @Test
    public void testUserProfileResponseNoArgConstructor() {
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        assertThat(userProfileResponse.getEmail()).isNull();
    }
}
