package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.Arrays;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileDataResponseTest {

    @Test
    public void test_UserProfileDataResponse() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        UserProfileDataResponse sut = new UserProfileDataResponse(Arrays.asList(userProfile, userProfile), false);

        assertThat(sut.getUserProfiles()).isNotNull();
        assertThat(sut.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    public void test_NoArgConstructor() {
        UserProfileDataResponse sut = new UserProfileDataResponse();
        assertThat(sut.getUserProfiles()).isNull();
    }
}
