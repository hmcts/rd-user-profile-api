package uk.gov.hmcts.reform.userprofileapi.helper;

import lombok.Setter;
import org.apache.commons.lang3.RandomStringUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                RandomStringUtils.secure().nextAlphabetic(20),
                RandomStringUtils.secure().nextAlphabetic(20),
                LanguagePreference.EN.toString(),
                false,
                false,
                UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(),
                getIdamRolesJson(),
                isReinviteUser);
    }

    public static UpdateUserProfileData buildUpdateUserProfileData() {
        return new UpdateUserProfileData(UUID.randomUUID().toString(),
                generateRandomEmail(),
                RandomStringUtils.secure().nextAlphabetic(10) + " " + RandomStringUtils.secure().nextAlphabetic(10),
                RandomStringUtils.secure().nextAlphabetic(10) + " " + RandomStringUtils.secure().nextAlphabetic(10),
                IdamStatus.ACTIVE.toString(),
                null, null
        );
    }

    public static String generateRandomEmail() {
        return String.format(EMAIL_TEMPLATE, RandomStringUtils.secure().nextAlphanumeric(10));
    }

    public static List<String> getIdamRolesJson() {
        List<String> roles = new ArrayList<>();
        roles.add("caseworker");
        return roles;
    }
}
