package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildUpdateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder.buildUserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class UpdateUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        UserProfile user = userProfileRepository.save(buildUserProfile());

        assertTrue(userProfileRepository.existsById(user.getId()));

        userProfileMap = new HashMap<>();
        userProfileMap.put("user", user);
    }

    @Test
    public void should_return_200_and_update_user_profile_resource() throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        String idamId = persistedUserProfile.getIdamId();
        UpdateUserProfileData data = buildUpdateUserProfileData();

        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + SLASH + idamId,
                data,
                OK
        );

        verifyUserProfileCreation(data, persistedUserProfile);

    }

    @Test
    public void should_return_200_and_when_IdamStatus_is_updated() throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        String idamId = persistedUserProfile.getIdamId();
        UpdateUserProfileData data = buildUpdateUserProfileData();
        data.setIdamStatus("Active");

        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + SLASH + idamId,
                data,
                OK
        );

        Optional<UserProfile> optionalUp = userProfileRepository.findByIdamId(persistedUserProfile.getIdamId());
        UserProfile updatedUserProfile = optionalUp.orElse(null);
        assertThat(updatedUserProfile.getStatus()).isEqualTo(IdamStatus.ACTIVE);

    }

    private void verifyUserProfileCreation(UpdateUserProfileData data, UserProfile persistedUserProfile) {

        Optional<UserProfile> optionalUp = userProfileRepository.findByIdamId(persistedUserProfile.getIdamId());
        UserProfile updatedUserProfile = optionalUp.orElse(null);

        assertThat(updatedUserProfile).isNotNull();
        assertThat(updatedUserProfile.getIdamId()).isEqualTo(persistedUserProfile.getIdamId());
        assertThat(updatedUserProfile.getEmail()).isEqualToIgnoringCase(data.getEmail());
        assertThat(updatedUserProfile.getFirstName()).isEqualTo(data.getFirstName());
        assertThat(updatedUserProfile.getLastName()).isEqualTo(data.getLastName());
        assertThat(updatedUserProfile.getLanguagePreference()).isEqualTo(persistedUserProfile.getLanguagePreference());
        assertThat(updatedUserProfile.getUserCategory()).isEqualTo(persistedUserProfile.getUserCategory());
        assertThat(updatedUserProfile.getUserType()).isEqualTo(persistedUserProfile.getUserType());
        assertThat(updatedUserProfile.getStatus().toString()).isEqualTo(data.getIdamStatus());
        assertThat(updatedUserProfile.isEmailCommsConsent()).isEqualTo(persistedUserProfile.isEmailCommsConsent());
        assertThat(updatedUserProfile.isPostalCommsConsent()).isEqualTo(persistedUserProfile.isPostalCommsConsent());
        assertThat(updatedUserProfile.getEmailCommsConsentTs()).isEqualTo(persistedUserProfile.getEmailCommsConsentTs());
        assertThat(updatedUserProfile.getPostalCommsConsentTs()).isEqualTo(persistedUserProfile.getPostalCommsConsentTs());
        assertThat(updatedUserProfile.getCreated()).isEqualTo(persistedUserProfile.getCreated());

        Optional<Audit> optional = auditRepository.findByUserProfile(updatedUserProfile);

        Audit audit = optional.get();
        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(200);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.OK);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.SYNC);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(updatedUserProfile.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }


    @Test
    public void should_return_400_while_update_profile_when_empty_body() throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        String idamId = persistedUserProfile.getIdamId();

        userProfileRequestHandlerTest.sendPut(
            mockMvc,
            APP_BASE_PATH + SLASH + idamId,
            "{}",
            BAD_REQUEST
        );
    }

    @Test
    public void should_return_404_while_create_user_profile_when_userId_invalid() throws Exception {

        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + SLASH + "invalid",
                "{}",
                NOT_FOUND
        );
    }

    @Test
    public void should_return_404_while_create_user_profile_when_userId_not_in_db() throws Exception {

        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + SLASH + UUID.randomUUID(),
                "{}",
                NOT_FOUND
        );
    }

    @Test
    public void should_return_400_when_any_mandatory_field_missing() throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        String idamId = persistedUserProfile.getIdamId();
        List<String> mandatoryFieldList =
            Lists.newArrayList(
                "email",
                "firstName",
                "lastName",
                "idamStatus"
            );

        new JSONObject(
            objectMapper.writeValueAsString(
                    buildCreateUserProfileData()
            )
        );

        mandatoryFieldList.forEach(s -> {

            try {

                JSONObject jsonObject =
                    new JSONObject(objectMapper.writeValueAsString(buildCreateUserProfileData()));

                jsonObject.remove(s);

                mockMvc.perform(put(APP_BASE_PATH + SLASH + idamId.toString())
                    .content(jsonObject.toString())
                    .contentType(APPLICATION_JSON))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

    //@Test
    public void should_return_200_and_update_user_profile_resource_with_valid_email() throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        String idamId = persistedUserProfile.getIdamId();
        UpdateUserProfileData data = buildUpdateUserProfileData();
        data.setEmail("a.adison@gmail.com");

        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + SLASH + idamId,
                data,
                OK
        );

    }
}
