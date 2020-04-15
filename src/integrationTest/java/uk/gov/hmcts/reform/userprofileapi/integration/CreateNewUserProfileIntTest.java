package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.List;
import java.util.Optional;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_return_201_and_create_user_profile_resource() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
            userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                CREATED,
                UserProfileCreationResponse.class
            );

        verifyUserProfileCreation(createdResource, CREATED, data);
    }

    private void verifyUserProfileCreation(UserProfileCreationResponse createdResource, HttpStatus idamStatus, UserProfileCreationData data) {

        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(String.class);
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


    @Test
    public void should_return_400_when_any_mandatory_field_missing() throws Exception {

        List<String> mandatoryFieldList =
            Lists.newArrayList(
                "email",
                "firstName",
                "lastName",
                "roles"
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

                mockMvc.perform(post(APP_BASE_PATH)
                    .content(jsonObject.toString())
                    .contentType(APPLICATION_JSON_VALUE))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

    @Test
    public void should_return_400_when_fields_are_blank_or_having_only_whitespaces() throws Exception {

        List<String> mandatoryFieldList =
                Lists.newArrayList(
                        "email",
                        "firstName",
                        "lastName",
                        "userCategory",
                        "userType"
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

                jsonObject.put(s,"");

                mockMvc.perform(post(APP_BASE_PATH)
                        .content(jsonObject.toString())
                        .contentType(APPLICATION_JSON_VALUE))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();

                jsonObject.put(s," ");

                mockMvc.perform(post(APP_BASE_PATH)
                        .content(jsonObject.toString())
                        .contentType(APPLICATION_JSON_VALUE))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

    @Test
    public void should_return_201_and_create_user_profile_resource_with_allowed_email() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        data.setEmail("a.adison@gmail.com");

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, data);
    }

    @Test
    public void should_return_400_and_create_user_profile_resource_with_invalid_email() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        data.setEmail("a.adisongmail.com");


        userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                BAD_REQUEST,
                UserProfileCreationResponse.class
        );

    }

    @Test
    public void should_return_400_and_create_user_profile_resource_with_invalid_email_1() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        data.setEmail("a.adison@gmailcom");


        userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                BAD_REQUEST,
                UserProfileCreationResponse.class
        );



    }

}
