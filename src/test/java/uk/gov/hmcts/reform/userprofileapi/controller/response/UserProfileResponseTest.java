package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

public class UserProfileResponseTest {

    @Test
    public void test_UserProfileResponse() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        UserProfileResponse userProfileResponse = new UserProfileResponse(userProfile);

        assertThat(userProfileResponse).isNotNull();
        assertThat(userProfileResponse.getIdamId()).isEqualTo(userProfile.getIdamId());
        assertThat(userProfileResponse.getEmail()).isEqualTo(userProfile.getEmail());
        assertThat(userProfileResponse.getFirstName()).isEqualTo(userProfile.getFirstName());
        assertThat(userProfileResponse.getLastName()).isEqualTo(userProfile.getLastName());
        assertThat(userProfileResponse.getIdamStatus()).isEqualTo(IdamStatus.PENDING.name());
    }

    @Test
    public void test_UserProfileResponseNoArgConstructor() {
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        assertThat(userProfileResponse.getEmail()).isNull();
    }
}
