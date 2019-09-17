package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileResourceTest {

    @Test
    public void should_create_successfully_with_no_args_constructor() {

        CreateUserProfileResponse userProfile = new CreateUserProfileResponse(new UserProfile());
        Arrays.asList(userProfile.getClass().getDeclaredMethods()).forEach(method -> {
            try {
                if (method.getName().startsWith("get")) {
                    assertThat(method.invoke(userProfile)).isNull();
                } else if (method.getName().startsWith("is")) {
                    assertThat((Boolean) method.invoke(userProfile)).isFalse();
                }
            } catch (Exception e) {
                Assertions.fail("unable to invoke method %s", method.getName());
            }
        });
    }

    @Test
    public void should_create_from_user_profile_successfully() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfileWithAllFields();
        CreateUserProfileResponse userProfileResource = new CreateUserProfileResponse(userProfile);

        assertThat(userProfileResource.getIdamId()).isNotNull();
        assertThat(userProfileResource.getIdamRegistrationResponse()).isEqualTo(userProfile.getIdamRegistrationResponse());
    }

}
