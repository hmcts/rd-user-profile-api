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
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildUpdateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

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

    @Test
    public void should_see_idam_error_message_and_when_IdamStatus_is_updated_by_exui_and_idam_fails() throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        setSidamUserUpdateMockWithStatus(NOT_FOUND.value(), false, persistedUserProfile.getIdamId());
        updateUserStatusAndVerify(persistedUserProfile, "Not Found", 404);

    }

    @Test
    public void should_see_idam_err_msg_and_when_IdamStatus_is_updated_by_exui_and_idam_failsand_does_not_give_body()
            throws Exception {

        UserProfile persistedUserProfile = userProfileMap.get("user");
        setSidamUserUpdateMockWithStatus(NOT_FOUND.value(), true, persistedUserProfile.getIdamId());
        updateUserStatusAndVerify(persistedUserProfile, "16 Resource not found", 404);

    }

    public void updateUserStatusAndVerify(UserProfile persistedUserProfile, String message, int errorCode)
            throws Exception {
        persistedUserProfile.setStatus(IdamStatus.ACTIVE);
        userProfileRepository.save(persistedUserProfile);
        String idamId = persistedUserProfile.getIdamId();
        UpdateUserProfileData data = buildUpdateUserProfileData();
        data.setIdamStatus("Active");
        UserProfileRolesResponse userProfileRolesResponse =  userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + SLASH + idamId + "?origin=EXUI",
                data,
                NOT_FOUND,
                UserProfileRolesResponse.class
        );

        assertThat(userProfileRolesResponse).isNotNull();
        assertThat(userProfileRolesResponse.getAttributeResponse().getIdamStatusCode()).isEqualTo(errorCode);
        assertThat(userProfileRolesResponse.getAttributeResponse().getIdamMessage()).isEqualTo(message);
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
        assertThat(updatedUserProfile.getEmailCommsConsentTs())
                .isEqualTo(persistedUserProfile.getEmailCommsConsentTs());
        assertThat(updatedUserProfile.getPostalCommsConsentTs())
                .isEqualTo(persistedUserProfile.getPostalCommsConsentTs());
        assertThat(updatedUserProfile.getCreated()).isEqualTo(persistedUserProfile.getCreated());

        List<Audit> matchedAuditRecords = getMatchedAuditRecords(auditRepository.findAll(),
                updatedUserProfile.getIdamId());
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);

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
                    .andExpect(status().is(BAD_REQUEST.value()))
                    .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

    @Test
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
