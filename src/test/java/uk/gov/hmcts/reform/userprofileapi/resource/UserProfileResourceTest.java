package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileResourceTest {

    @Test
    public void should_represent_same_number_of_fields_as_in_db() {
        long numberOfFieldsInDb = 20;
        long fieldCount = Stream.of(UserProfile.class.getDeclaredFields())
            .filter(field -> !field.getName().startsWith("$"))
            .map(Field::getName)
            .count();

        assertThat(fieldCount).isEqualTo(numberOfFieldsInDb);
    }

    @Test
    public void should_represent_same_number_of_fields() {
        long numberOfFieldsInDb = 2;
        long fieldCount = Stream.of(UserProfileCreationResponse.class.getDeclaredFields())
                .filter(field -> !field.getName().startsWith("$"))
                .map(Field::getName)
                .count();

        assertThat(fieldCount).isEqualTo(numberOfFieldsInDb);
    }

//    @Test
//    public void should_create_successfully_with_no_args_constructor() {
//
//        UserProfileCreationResponse userProfile = new UserProfileCreationResponse(new UserProfile());
//        Arrays.asList(userProfile.getClass().getDeclaredMethods()).forEach(method -> {
//            try {
//                if (method.getName().startsWith("get")) {
//                    assertThat(method.invoke(userProfile)).isNull();
//                } else if (method.getName().startsWith("is")) {
//                    assertThat((Boolean) method.invoke(userProfile)).isFalse();
//                }
//            } catch (Exception e) {
//                Assertions.fail("unable to invoke method %s", method.getName());
//            }
//        });
//    }

    @Test
    public void should_create_from_user_profile_successfully() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfileWithAllFields();
        UserProfileCreationResponse userProfileResource = new UserProfileCreationResponse(userProfile);

        assertThat(userProfileResource.getIdamId()).isNotNull();
        assertThat(userProfileResource.getIdamRegistrationResponse()).isEqualTo(userProfile.getIdamRegistrationResponse());
    }

}
