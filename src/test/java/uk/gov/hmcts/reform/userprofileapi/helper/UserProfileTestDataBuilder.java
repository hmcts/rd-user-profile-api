package uk.gov.hmcts.reform.userprofileapi.helper;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

public class UserProfileTestDataBuilder {

    private UserProfileTestDataBuilder() {
        //do not want to instantiate
    }

    public static UserProfile buildUserProfile() {
        UserProfile up = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);
        up.setIdamId(UUID.randomUUID().toString());
        return up;
    }

    public static UserProfile buildUserProfileWithDeletedStatus() {
        UserProfile up = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);
        up.setStatus(IdamStatus.DELETED);
        up.setIdamId(UUID.randomUUID().toString());
        return up;
    }

    public static UserProfile buildUserProfileWithSuspendedStatus() {
        UserProfile up = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);
        up.setStatus(IdamStatus.SUSPENDED);
        up.setIdamId(UUID.randomUUID().toString());
        return up;
    }

    public static UserProfile buildUserProfileWithAllFields() {

        List<String> unInitFields = List.of("id", "idamId", "status", "created", "lastUpdated");

        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        unInitFields.forEach(fieldName -> {

            Field field;
            try {
                field = userProfile.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);

                if (field.getType().equals(UUID.class)) {
                    field.set(userProfile, UUID.randomUUID());
                } else if ((field.getType().equals(String.class))) {
                    field.set(userProfile, RandomStringUtils.secure().nextAlphanumeric(32));
                } else if ((field.getType().equals(LocalDateTime.class))) {
                    field.set(userProfile, LocalDateTime.now());
                }
            } catch (Exception e) {
                throw new IllegalStateException("could not set field value ", e);
            }

        });

        return userProfile;

    }

    public UserProfile buildUserProfileWithAnIdamId() {
        UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

        Field idamIdField;
        try {
            idamIdField = userProfile.getClass().getDeclaredField("idamId");
            idamIdField.setAccessible(true);
            idamIdField.set(userProfile, RandomStringUtils.secure().nextAlphanumeric(32));
        } catch (Exception e) {
            throw new IllegalStateException("could not set idamId field value", e);
        }

        return userProfile;
    }

}
