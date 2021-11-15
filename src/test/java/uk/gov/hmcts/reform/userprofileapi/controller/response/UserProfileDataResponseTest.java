package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

class UserProfileDataResponseTest {

    @Test
    void test_UserProfileDataResponse() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        UserProfileDataResponse sut = new UserProfileDataResponse(Arrays.asList(userProfile, userProfile), false);

        assertThat(sut.getUserProfiles()).isNotNull();
        assertThat(sut.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    void test_NoArgConstructor() {
        UserProfileDataResponse sut = new UserProfileDataResponse();
        assertThat(sut.getUserProfiles()).isNull();
    }
}
