package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;


class UserProfileCreationResponseTest {

    @Test
    void test_hold_values_after_creation() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        UserProfileCreationResponse sut = new UserProfileCreationResponse(userProfile);

        assertThat(sut.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(201);
    }

    @Test
    void test_UserProfileCreationResponseNoArg() {
        UserProfileCreationResponse sut = new UserProfileCreationResponse();
        assertThat(sut).isNotNull();
    }
}
