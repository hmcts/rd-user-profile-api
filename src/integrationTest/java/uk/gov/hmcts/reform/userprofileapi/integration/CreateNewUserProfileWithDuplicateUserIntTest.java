package uk.gov.hmcts.reform.userprofileapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
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

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

class CreateNewUserProfileWithDuplicateUserIntTest extends AuthorizationEnabledIntegrationTest {


    @BeforeEach
    public void setUpWireMock() {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        idamMockService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/v1/users/" + "7feb739c-1ae1-4ef4-9f46-86716d84fd72")
                        .withStatus(409)
                ));
    }

    public void mockWithGetSuccess(boolean withoutStatusFields) throws JsonProcessingException {

        HashMap<Object,Object> data;
        if (!withoutStatusFields) {
            data = new HashMap<>();
            data.put("active", "true");
            data.put("forename","fname");
            data.put("surname","lname");
            data.put("email","test@test.com");
            data.put("roles",List.of("pui-organisation-manager","pui-user-manager"));
        } else {
            data = new HashMap<>();
            data.put("id", "e65e5439-a8f7-4ae6-b378-cc1015b72dbb");
            data.put("active","false");
            data.put("pending","true");
        }

        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsString(data))));

    }

    public void mockWithUpdateSuccess() {
        idamMockService.stubFor(post(urlMatching("/api/v1/users/7feb739c-1ae1-4ef4-9f46-86716d84fd72/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public void mockWithGetFail() {
        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(404)
                ));

    }

    public void mockWithUpdateFail() {
        idamMockService.stubFor(post(urlMatching("/api/v1/users/7feb739c-1ae1-4ef4-9f46-86716d84fd72/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                ));
    }

    @Test
    void should_return_201_and_create_user_profile_when_duplicate_in_sidam() throws Exception {

        mockWithGetSuccess(false);
        mockWithUpdateSuccess();
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, IdamStatus.ACTIVE);

    }

    @Test
    void should_return_201_and_create_user_profile_when_status_not_properly_returned_by_sidam()
            throws Exception {

        mockWithGetSuccess(false);
        mockWithUpdateSuccess();
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, IdamStatus.ACTIVE);

    }

    @Test
    void should_return_404_and_not_create_user_profile_when_duplicate_in_sidam_and_get_failed()
            throws Exception {

        mockWithGetFail();
        mockWithUpdateSuccess();
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
        UserProfileCreationData data = buildCreateUserProfileData();


        userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                NOT_FOUND,
                UserProfileCreationResponse.class
        );

        verifyUserProfileCreationForFailure(NOT_FOUND);

    }

    @Test
    void should_return_400_and_not_create_user_profile_when_duplicate_in_sidam_and_update_failed()
            throws Exception {

        mockWithGetSuccess(true);
        mockWithUpdateFail();
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreationForFailure(BAD_REQUEST);

    }

    private void verifyUserProfileCreation(UserProfileCreationResponse createdResource, HttpStatus idamStatus,
                                           IdamStatus expectedIdamStatus) {

        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(String.class);
        assertThat(createdResource.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByIdamId(createdResource.getIdamId());
        UserProfile userProfile = persistedUserProfile.get();
        assertThat(userProfile.getId()).isNotNull().isExactlyInstanceOf(Long.class);
        assertThat(userProfile.getIdamRegistrationResponse()).isNull();
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.getUserCategory()).isEqualTo(UserCategory.PROFESSIONAL);
        assertThat(userProfile.getUserType()).isEqualTo(UserType.EXTERNAL);
        assertThat(userProfile.getStatus()).isEqualTo(expectedIdamStatus);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
        assertThat(userProfile.getCreated()).isNotNull();
        assertThat(userProfile.getLastUpdated()).isNotNull();

        List<Audit> matchedAudit = getMatchedAuditRecords(auditRepository.findAll(), userProfile.getIdamId());
        assertThat(matchedAudit.size()).isEqualTo(1);
        Audit audit = matchedAudit.get(0);
        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.ACCEPTED);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(createdResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }

    private void verifyUserProfileCreationForFailure(HttpStatus idamStatus) {

        Iterable<UserProfile> userProfileList = userProfileRepository.findAll();
        assertThat(userProfileList.iterator().hasNext()).isFalse();

        List<Audit> auditList = auditRepository.findAll();
        assertThat(auditList.size()).isEqualTo(1);
        Audit audit = auditList.get(0);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus));
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile()).isNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

}
