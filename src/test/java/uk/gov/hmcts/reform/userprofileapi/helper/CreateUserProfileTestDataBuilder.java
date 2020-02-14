package uk.gov.hmcts.reform.userprofileapi.helper;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@Setter
public class CreateUserProfileTestDataBuilder {

    private CreateUserProfileTestDataBuilder() {
        //not meant to be instantiated.
    }

    public static UserProfileCreationData buildCreateUserProfileData() {
        return new UserProfileCreationData(
            buildRandomEmail(),
            RandomStringUtils.randomAlphabetic(20),
            RandomStringUtils.randomAlphabetic(20),
            LanguagePreference.EN.toString(),
            false,
            false,
            UserCategory.PROFESSIONAL.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson());
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

    public static UpdateUserProfileData buildUpdateUserProfileDataForUpdatingStatus() {
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
        List<String> roles = new ArrayList<String>();
        roles.add("caseworker");
        return roles;
    }

}
