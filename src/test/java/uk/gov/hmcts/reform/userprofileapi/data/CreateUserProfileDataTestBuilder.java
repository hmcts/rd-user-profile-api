package uk.gov.hmcts.reform.userprofileapi.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.*;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

public class CreateUserProfileDataTestBuilder {

    private CreateUserProfileDataTestBuilder() {
        //not meant to be instantiated.
    }

    public static UserProfileCreationData buildCreateUserProfileData() {
        return new UserProfileCreationData(buildRandomEmail(), RandomStringUtils.randomAlphabetic(20),
                RandomStringUtils.randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false, UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson());
    }

    public static UpdateUserProfileData buildUpdateUserProfileData() {
        return new UpdateUserProfileData(
                buildRandomEmail(),
                RandomStringUtils.randomAlphabetic(20),
                RandomStringUtils.randomAlphabetic(20),
                IdamStatus.ACTIVE.toString(),
                null,null
                );
    }

    private static String buildRandomEmail() {
        return RandomStringUtils.randomAlphanumeric(20) + "@somewhere.com";
    }

    public static List<String> getIdamRolesJson() {
        List<String> roles = new ArrayList<>();
        roles.add("caseworker");
        return roles;
    }

}
