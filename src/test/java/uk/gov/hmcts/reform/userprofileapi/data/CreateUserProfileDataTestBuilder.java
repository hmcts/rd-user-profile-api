package uk.gov.hmcts.reform.userprofileapi.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import lombok.Setter;
import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@Setter
public class CreateUserProfileDataTestBuilder {

    private CreateUserProfileDataTestBuilder() {
        //not meant to be instantiated.
    }

    public static CreateUserProfileData buildCreateUserProfileData() {
        return new CreateUserProfileData(
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
                new HashSet<>()
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
