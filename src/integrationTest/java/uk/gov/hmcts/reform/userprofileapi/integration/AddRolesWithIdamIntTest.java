package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder.buildUserProfile;


public class AddRolesWithIdamIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;

    @Test
    public void should_return_200_and_add_roles_to_user_profile_resource() throws Exception {

        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        CreateUserProfileResponse.class
                );

        CreateUserProfileResponse createdResource1 =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data.getEmail(),
                        CREATED,
                        CreateUserProfileResponse.class
                );

        UserProfile persistedUserProfile = userProfileMap.get("user");
        String idamId = persistedUserProfile.getIdamId();

//        UserProfile user = buildUserProfile();
//        user.setIdamId("1234567");
//        user.setStatus(IdamStatus.ACTIVE);
//        testUserProfileRepository.save(user);
//
//        UpdateUserProfileData data = new UpdateUserProfileData();
//
//        RoleName role1 = new RoleName("pui-case-manager");
//        RoleName role2 = new RoleName("prd-Admin");
//
//        List<RoleName> roles = new ArrayList<>();
//        roles.add(role1);
//        roles.add(role2);
//
//        data.setRoles(roles);


        GetUserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?" + "userId=" + "1234567",
                        OK,
                        GetUserProfileResponse.class
                );

        assertThat(retrievedResource).isNotNull();
    }


    private void verifyUserProfileCreation(CreateUserProfileResponse createdResource, HttpStatus idamStatus, UpdateUserProfileData data) {

        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(UUID.class);
        assertThat(createdResource.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByIdamId(createdResource.getIdamId());
        UserProfile userProfile = persistedUserProfile.get();
        assertThat(userProfile.getId()).isNotNull().isExactlyInstanceOf(Long.class);
        assertThat(userProfile.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(userProfile.getEmail()).isEqualToIgnoringCase(data.getEmail());
        assertThat(userProfile.getFirstName()).isNotEmpty().isEqualTo(data.getFirstName());
        assertThat(userProfile.getLastName()).isNotEmpty().isEqualTo(data.getLastName());
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.getUserCategory()).isEqualTo(UserCategory.PROFESSIONAL);
        assertThat(userProfile.getUserType()).isEqualTo(UserType.EXTERNAL);
        assertThat(userProfile.getStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(userProfile.isEmailCommsConsent()).isEqualTo(false);
        assertThat(userProfile.isPostalCommsConsent()).isEqualTo(false);
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
        assertThat(userProfile.getCreated()).isNotNull();
        assertThat(userProfile.getLastUpdated()).isNotNull();

        Optional<Audit> optional = auditRepository.findByUserProfile(userProfile);
        Audit audit = optional.get();

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.ACCEPTED);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(createdResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }


    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body() throws Exception {

        MvcResult result =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        "{}",
                        BAD_REQUEST
                );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

}
