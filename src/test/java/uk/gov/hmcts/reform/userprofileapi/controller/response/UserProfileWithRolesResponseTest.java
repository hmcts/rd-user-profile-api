package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

public class UserProfileWithRolesResponseTest {

    private UserProfileWithRolesResponse sut;

    private final String dummyEmail = "a@hmcts.net";

    private final String dummyFirstName = "april";

    private final String dummyLastName = "o'neil";

    private final String dummyStatusCode = "200";

    private final String dummyErrorMessage = "Resource Not Found";

    private String dummyIdamId;

    private UserProfile userProfileMock = Mockito.mock(UserProfile.class);

    private IdamRolesInfo idamRolesInfoMock = Mockito.mock(IdamRolesInfo.class);

    private List<String> dummyRoles = new ArrayList<>();

    @Before
    public void setUp() {
        dummyRoles.add("prd-admin");

        dummyIdamId = UUID.randomUUID().toString();

        when(userProfileMock.getIdamId()).thenReturn(dummyIdamId);
        when(userProfileMock.getIdamRegistrationResponse()).thenReturn(201);
        when(userProfileMock.getFirstName()).thenReturn(dummyFirstName);
        when(userProfileMock.getLastName()).thenReturn(dummyLastName);
        when(userProfileMock.getEmail()).thenReturn(dummyEmail);
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.ACTIVE);
        when(userProfileMock.getErrorStatusCode()).thenReturn(dummyStatusCode);
        when(userProfileMock.getErrorMessage()).thenReturn(dummyErrorMessage);
        when(userProfileMock.getRoles()).thenReturn(dummyRoles);
        when(idamRolesInfoMock.getRoles()).thenReturn(dummyRoles);
    }

    @Test
    public void testUserProfileWithRolesResponse() {
        sut = new UserProfileWithRolesResponse(userProfileMock, true);

        assertThat(sut).isNotNull();
        assertThat(sut.getIdamId()).isEqualTo(dummyIdamId);
        assertThat(sut.getEmail()).isEqualTo(dummyEmail);
        assertThat(sut.getFirstName()).isEqualTo(dummyFirstName);
        assertThat(sut.getLastName()).isEqualTo(dummyLastName);
        assertThat(sut.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(sut.getIdamMessage()).isEqualTo(dummyErrorMessage);
        assertThat(sut.getIdamStatusCode()).isEqualTo(dummyStatusCode);
        assertThat(sut.getRoles()).isEqualTo(dummyRoles);
    }

    @Test
    public void testUserProfileWithRolesNotRequired() {
        sut = new UserProfileWithRolesResponse(userProfileMock, false);

        assertThat(sut.getRoles()).isNull();
        assertThat(sut.getIdamStatusCode()).isEqualTo(" ");
        assertThat(sut.getIdamMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
    }

    @Test
    public void testUserProfileWithRolesResponseStatusPending() {
        when(userProfileMock.getStatus()).thenReturn(IdamStatus.PENDING);

        sut = new UserProfileWithRolesResponse(userProfileMock, true);

        assertThat(sut).isNotNull();
        assertThat(sut.getIdamId()).isEqualTo(dummyIdamId);
        assertThat(sut.getEmail()).isEqualTo(dummyEmail);
        assertThat(sut.getFirstName()).isEqualTo(dummyFirstName);
        assertThat(sut.getLastName()).isEqualTo(dummyLastName);
        assertThat(sut.getIdamStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(sut.getRoles()).isNull();
        assertThat(sut.getIdamMessage()).isEqualTo(dummyErrorMessage);
        assertThat(sut.getIdamStatusCode()).isEqualTo("200");
    }

}
