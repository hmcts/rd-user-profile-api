package uk.gov.hmcts.reform.userprofileapi.data;

import org.apache.commons.lang.RandomStringUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;

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
            UserType.INTERNAL.toString(),
            getIdamRolesJson());
    }

    public static CreateUserProfileData buildCreateUserProfileDataMandatoryFieldsOnly() {
        return new CreateUserProfileData(
            buildRandomEmail(),
            RandomStringUtils.randomAlphabetic(20),
            RandomStringUtils.randomAlphabetic(20),
            null,

            false,
            false,

            UserCategory.PROFESSIONAL.toString(),
            UserType.INTERNAL.toString(),
            null);
    }

    private static String buildRandomEmail() {
        return RandomStringUtils.randomAlphanumeric(20) + "@somewhere.com";
    }

    public static String getIdamRolesJson() {
        return "{\"roles\":\"role\": \"some-role\"}";
    }

}
