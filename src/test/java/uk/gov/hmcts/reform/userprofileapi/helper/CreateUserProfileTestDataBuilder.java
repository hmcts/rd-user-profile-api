package uk.gov.hmcts.reform.userprofileapi.helper;

import static org.apache.commons.lang.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@Setter
public class CreateUserProfileTestDataBuilder {

    public static final String EMAIL_TEMPLATE = "freg-test-user-%s@prdfunctestuser.com";

    private CreateUserProfileTestDataBuilder() {
        //not meant to be instantiated.
    }

    public static UserProfileCreationData buildCreateUserProfileData() {
        return buildCreateUserProfileData(false);
    }

    public static UserProfileCreationData buildCreateUserProfileData(boolean isReinviteUser) {
        return new UserProfileCreationData(
                generateRandomEmail(),
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
                generateRandomEmail(),
                randomAlphabetic(10) + " " + randomAlphabetic(10),
                randomAlphabetic(10) + " " + randomAlphabetic(10),
                IdamStatus.ACTIVE.toString(),
                null,null
                );
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, randomAlphanumeric(10));
    }

    public static List<String> getIdamRolesJson() {
        List<String> roles = new ArrayList<String>();
        roles.add("caseworker");
        return roles;
    }

}
