package uk.gov.hmcts.reform.userprofileapi.helper;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

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
        return buildCreateUserProfileData(false);
    }

    public static UserProfileCreationData buildCreateUserProfileData(boolean isReinviteUser) {
        return new UserProfileCreationData(
            buildRandomEmail(),
            randomAlphabetic(20),
            randomAlphabetic(20),
            LanguagePreference.EN.toString(),
            false,
            false,
            UserCategory.PROFESSIONAL.toString(),
            UserType.EXTERNAL.toString(),
            getIdamRolesJson(),
                isReinviteUser);
    }

    public static UpdateUserProfileData buildUpdateUserProfileData() {
        return new UpdateUserProfileData(
                buildRandomEmail(),
                randomAlphabetic(10) + " " + randomAlphabetic(10),
                randomAlphabetic(10) + " " + randomAlphabetic(10),
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
