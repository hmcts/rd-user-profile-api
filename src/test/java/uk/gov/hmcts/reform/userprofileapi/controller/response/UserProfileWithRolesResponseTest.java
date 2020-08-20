package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

public class UserProfileWithRolesResponseTest {

    private UserProfileWithRolesResponse sut;

    private final String dummyEmail = "a@hmcts.net";
    private final String dummyFirstName = "april";
    private final String dummyLastName = "o'neil";
    private final String dummyStatusCode = "200";
    private final String dummyErrorMessage = "Resource Not Found";
    private String dummyIdamId;
    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);
    private IdamRolesInfo idamRolesInfoMock = Mockito.mock(IdamRolesInfo.class);
    private List<String> dummyRoles = new ArrayList<>();

    @Before
    public void setUp() {
        dummyRoles.add("prd-admin");
        dummyIdamId = UUID.randomUUID().toString();

        when(idamRolesInfoMock.getRoles()).thenReturn(dummyRoles);

        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setRoles(idamRolesInfoMock);
    }

    @Test
    public void test_UserProfileWithRolesResponse() {
        sut = new UserProfileWithRolesResponse(userProfile, true);

        assertThat(sut).isNotNull();
        assertThat(sut.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(sut.getEmail()).isEqualTo(userProfile.getEmail());
        assertThat(sut.getFirstName()).isEqualTo(userProfile.getFirstName());
        assertThat(sut.getLastName()).isEqualTo(userProfile.getLastName());
        assertThat(sut.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
        assertThat(sut.getIdamMessage()).isEqualTo(userProfile.getErrorMessage());
        assertThat(sut.getIdamStatusCode()).isEqualTo(userProfile.getErrorStatusCode());
        assertThat(sut.getRoles()).isEqualTo(dummyRoles);
    }

    @Test
    public void test_UserProfileWithRolesNotRequired() {
        sut = new UserProfileWithRolesResponse(userProfile, false);

        assertThat(sut.getRoles()).isNull();
        assertThat(sut.getIdamStatusCode()).isEqualTo(" ");
        assertThat(sut.getIdamMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
    }

    @Test
    public void test_UserProfileWithRolesResponseStatusPending() {
        userProfile.setStatus(IdamStatus.PENDING);

        sut = new UserProfileWithRolesResponse(userProfile, true);

        assertThat(sut).isNotNull();
        assertThat(sut.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(sut.getEmail()).isEqualTo(userProfile.getEmail());
        assertThat(sut.getFirstName()).isEqualTo(userProfile.getFirstName());
        assertThat(sut.getLastName()).isEqualTo(userProfile.getLastName());
        assertThat(sut.getIdamStatus()).isEqualTo(IdamStatus.PENDING.name());
        assertThat(sut.getRoles()).isNull();
        assertThat(sut.getIdamMessage()).isEqualTo(userProfile.getErrorMessage());
        assertThat(sut.getIdamStatusCode()).isEqualTo(userProfile.getErrorStatusCode());
    }

}
