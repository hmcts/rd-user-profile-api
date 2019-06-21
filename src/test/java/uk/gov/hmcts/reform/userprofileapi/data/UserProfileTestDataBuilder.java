package uk.gov.hmcts.reform.userprofileapi.data;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.util.Lists;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public class UserProfileTestDataBuilder {

    private UserProfileTestDataBuilder() {
        //do not want to instantiate
    }

    public static UserProfile buildUserProfile() {
        return new UserProfile(buildCreateUserProfileData(),
            new IdamRegistrationInfo(HttpStatus.CREATED));
    }

    public static UserProfile buildUserProfileWithAllFields() {

        List<String> unInitFields =
            Lists.newArrayList("id", "idamId", "idamStatus", "createdTs", "lastUpdatedTs");

        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(),
            new IdamRegistrationInfo(HttpStatus.CREATED));

        unInitFields.forEach(fieldName -> {

            Field field;
            try {
                field = userProfile.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                if (field.getType().equals(UUID.class)) {
                    System.out.println("Setting UUID >>>>>> ");
                    field.set(userProfile, UUID.randomUUID());
                } else if ((field.getType().equals(String.class))) {
                    System.out.println("Setting " + field.getName());
                    field.set(userProfile, randomAlphanumeric(32));
                } else if ((field.getType().equals(LocalDateTime.class))) {
                    System.out.println("Setting " + field.getName());
                    field.set(userProfile, LocalDateTime.now());
                }


            } catch (Exception e) {
                throw new IllegalStateException("could not set field value ", e);
            }

        });

        return userProfile;

    }

    public static UserProfile buildUserProfileWithAnIdamId() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(),
            new IdamRegistrationInfo(HttpStatus.CREATED));

        Field idamIdField;
        try {
            idamIdField = userProfile.getClass().getDeclaredField("idamId");
            idamIdField.setAccessible(true);
            idamIdField.set(userProfile, randomAlphanumeric(32));
        } catch (Exception e) {
            throw new IllegalStateException("could not set idamId field value", e);
        }

        return userProfile;
    }

}
