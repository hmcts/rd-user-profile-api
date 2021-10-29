package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

class UserProfileWithRolesResponseTest {

    private UserProfileWithRolesResponse sut;

    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);
    private final IdamRolesInfo idamRolesInfoMock = Mockito.mock(IdamRolesInfo.class);
    private final List<String> dummyRoles = new ArrayList<>();

    @BeforeEach
    public void setUp() {
        dummyRoles.add("prd-admin");

        when(idamRolesInfoMock.getRoles()).thenReturn(dummyRoles);

        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setRoles(idamRolesInfoMock);
    }

    @Test
    void test_UserProfileWithRolesResponse() {
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
    void test_UserProfileWithRolesNotRequired() {
        sut = new UserProfileWithRolesResponse(userProfile, false);

        assertThat(sut.getRoles()).isNull();
        assertThat(sut.getIdamStatusCode()).isEqualTo(" ");
        assertThat(sut.getIdamMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
    }

    @Test
    void test_UserProfileWithRolesResponseStatusPending() {
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
