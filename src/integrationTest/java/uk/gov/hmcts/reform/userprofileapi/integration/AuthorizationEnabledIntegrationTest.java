package uk.gov.hmcts.reform.userprofileapi.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRequestHandlerTest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.integration.wiremock.WireMockExtension;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.impl.FeatureToggleServiceImpl;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Configuration
@DirtiesContext
public abstract class AuthorizationEnabledIntegrationTest {

    protected static final String APP_BASE_PATH = "/v1/userprofile";
    protected static final String SLASH = "/";

    @Autowired
    protected UserProfileRepository userProfileRepository;

    @Autowired
    protected AuditRepository auditRepository;

    @Autowired
    protected UserProfileRequestHandlerTest userProfileRequestHandlerTest;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockBean
    protected FeatureToggleServiceImpl featureToggleService;

    @RegisterExtension
    protected WireMockExtension idamMockService = new WireMockExtension(5000);

    @RegisterExtension
    protected WireMockExtension s2sMockService = new WireMockExtension(8990);

    @BeforeEach
    public void setUpWireMock() {

        s2sMockService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_user_profile_api")));


        setSidamRegistrationMockWithStatus(HttpStatus.CREATED.value(), true);

        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"true\","
                                + "  \"forename\": \"Super\","
                                + "  \"surname\": \"User\","
                                + "  \"email\": \"test@test.com\","
                                + "  \"pending\": \"false\","
                                + "  \"roles\": ["
                                + "    \"pui-organisation-manager\""
                                + "  ]"
                                + "}")));

        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"false\","
                                + "  \"forename\": \"Suspended\","
                                + "  \"surname\": \"User\","
                                + "  \"email\": \"test@test.com\","
                                + "  \"pending\": \"false\","
                                + "  \"roles\": ["
                                + "    \"pui-organisation-manager\""
                                + "  ]"
                                + "}")));

        idamMockService.stubFor(delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(204)
                        .withBody("{"
                                + "  \"response\": \"User deleted successfully.\""
                                + "}")));

        idamMockService.stubFor(get(urlEqualTo("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                + "  \"uid\": \"%s\","
                                + "  \"name\": \"Super\","
                                + "  \"family_name\": \"User\","
                                + "  \"given_name\": \"User\","
                                + "  \"sub\": \"test@test.com\","
                                + "  \"accountStatus\": \"active\","
                                + "  \"roles\": ["
                                + "  \"pui-user-manager\""
                                + "  ]"
                                + "}")
                ));

        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(true);

    }

    protected void setSidamUserUpdateMockWithStatus(int status, boolean setBodyEmpty, String idamId) {
        String body = null;
        if (status == 404 && !setBodyEmpty) {
            body = "{"
                    + "\"status\": \"404\","
                    + "\"errorMessage\": \"Not Found\""
                    + "}";
        }
        idamMockService.stubFor(patch(urlMatching("/api/v1/users/" + idamId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(status)
                        .withBody(body)
                ));
    }

    protected void setSidamRegistrationMockWithStatus(int status, boolean setBodyEmpty) {
        String body = null;
        if (status == 400 && !setBodyEmpty) {
            body = "{"
                    + "\"status\": \"400\","
                    + "\"errorMessages\": ["
                    + "\"Role to be assigned does not exist.\""
                    + "]"
                    + "}";
        } else if (status == 409 && !setBodyEmpty) {
            body = "{"
                    + "\"status\": \"409\","
                    + "\"errorMessages\": ["
                    + "\"A user is already registered with this email.\""
                    + "]"
                    + "}";
        } else if (status == 404 && !setBodyEmpty) {
            body = "{"
                    + "\"status\": \"404\","
                    + "\"errorMessage\": \"16 Resource not found\","
                    + "\"errorDescription\": \"The role to be assigned does not exist.\""
                    + "}";
        }
        idamMockService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/v1/users/7f3c076c-e954-4d6f-80f7-6292160bf0bc")
                        .withStatus(status)
                        .withBody(body)
                ));
    }

    public void mockWithGetFail(HttpStatus httpStatus, boolean isBodyRequired) {
        String body = null;
        if (httpStatus == HttpStatus.NOT_FOUND && isBodyRequired) {
            body = "{"
                    + "\"status\": \"404\","
                    + "\"errorMessages\": ["
                    + "\"The user could not be found: c5d631f-af11-4816-abbe-ac6fd9b99ee9\""
                    + "]"
                    + "}";
        }
        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));

    }

    public void healthEndpointMock() {
        s2sMockService.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                + "\"status\": \"UP\""
                                + "}")));
    }

    protected UserProfileDataResponse getMultipleUsers(UserProfileDataRequest request, HttpStatus expectedStatus,
                                                       String showDeleted, String rolesRequired) throws Exception {
        return userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH + SLASH + "users?showdeleted=" + showDeleted + "&rolesRequired=" + rolesRequired,
                request,
                expectedStatus,
                UserProfileDataResponse.class
        );
    }

    protected Object createUser(UserProfileCreationData data, HttpStatus expectedStatus, Class clazz)
            throws Exception {
        return userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                expectedStatus,
                clazz
        );
    }

    protected void verifyUserProfileCreation(UserProfileCreationResponse createdResource, HttpStatus idamStatus,
                                             UserProfileCreationData data) {

        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(String.class);
        assertThat(createdResource.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByIdamId(createdResource.getIdamId());
        UserProfile userProfile = persistedUserProfile.get();
        assertThat(userProfile.getId()).isNotNull().isExactlyInstanceOf(Long.class);
        assertThat(userProfile.getEmail()).isEqualToIgnoringCase(data.getEmail());
        assertThat(userProfile.getFirstName()).isNotEmpty().isEqualTo(data.getFirstName());
        assertThat(userProfile.getLastName()).isNotEmpty().isEqualTo(data.getLastName());
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.getUserCategory()).isEqualTo(UserCategory.PROFESSIONAL);
        assertThat(userProfile.getUserType()).isEqualTo(UserType.EXTERNAL);
        assertThat(userProfile.getStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
        assertThat(userProfile.getCreated()).isNotNull();
        assertThat(userProfile.getLastUpdated()).isNotNull();

        List<Audit> audits = auditRepository.findAll();

        getMatchedAuditRecords(audits, userProfile.getIdamId()).forEach(audit -> verifyAudit(audit, createdResource));

    }

    public void verifyAudit(Audit audit, UserProfileCreationResponse createdResource) {
        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.ACCEPTED);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(createdResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();
    }

    @AfterEach
    public void tearDown() {
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    public static List<Audit> getMatchedAuditRecords(List<Audit> audits, String idamId) {
        return audits.stream().filter(audit -> audit.getUserProfile().getIdamId().equalsIgnoreCase(idamId))
                .collect(Collectors.toList());
    }

    public static UserProfileDataRequest buildUserProfileDataRequest(List<String> userIds) {
        return new UserProfileDataRequest(userIds);
    }

    public UserProfileCreationResponse createUserProfile(UserProfileCreationData data) throws Exception {

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, data);
        return createdResource;
    }

    public void createAndDeleteSingleUserProfile(UserProfileCreationData data) throws Exception {


        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, data);
        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource.getIdamId());
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        userProfileRequestHandlerTest.sendDelete(mockMvc,
                APP_BASE_PATH,
                deletionRequest,
                NO_CONTENT,
                UserProfilesDeletionResponse.class);

    }

    public void deleteUserProfiles(List<String> userIds, HttpStatus status) throws Exception {

        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        userProfileRequestHandlerTest.sendDelete(mockMvc,
                APP_BASE_PATH,
                deletionRequest,
                status,
                UserProfilesDeletionResponse.class);

    }
}
