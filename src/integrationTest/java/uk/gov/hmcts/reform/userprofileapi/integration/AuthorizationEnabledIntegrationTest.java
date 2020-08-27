package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
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
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@Configuration
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Integration")})
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990","IDAM_URL:http://127.0.0.1:5000"})
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

    @ClassRule
    public  static WireMockRule s2sService = new WireMockRule(8990);

    @ClassRule
    public  static WireMockRule idamService = new WireMockRule(5000);

    @Before
    public void setUpWireMock() {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_user_profile_api")));


        setSidamRegistrationMockWithStatus(HttpStatus.CREATED.value(), true);

        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"true\","
                                + "  \"forename\": \"Super\","
                                + "  \"surname\": \"User\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"pending\": \"false\","
                                + "  \"roles\": ["
                                + "    \"pui-organisation-manager\""
                                + "  ]"
                                + "}")));

        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"false\","
                                + "  \"forename\": \"Suspended\","
                                + "  \"surname\": \"User\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"pending\": \"false\","
                                + "  \"roles\": ["
                                + "    \"pui-organisation-manager\""
                                + "  ]"
                                + "}")));

        idamService.stubFor(get(urlEqualTo("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"uid\": \"%s\","
                                +  "  \"name\": \"Super\","
                                +  "  \"family_name\": \"User\","
                                +  "  \"given_name\": \"User\","
                                +  "  \"sub\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  \"pui-user-manager\""
                                +  "  ]"
                                +  "}")
                        ));

    }

    protected void setSidamUserUpdateMockWithStatus(int status, boolean setBodyEmpty, String idamId) {
        String body = null;
        if (status == 404 && !setBodyEmpty) {
            body = "{"
                    + "\"status\": \"404\","
                    + "\"errorMessage\": \"Not Found\""
                    + "}";
        }
        idamService.stubFor(patch(urlMatching("/api/v1/users/" + idamId))
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
        } else  if (status == 409 && !setBodyEmpty) {
            body = "{"
                    + "\"status\": \"409\","
                    + "\"errorMessages\": ["
                    + "\"A user is already registered with this email.\""
                    + "]"
                    + "}";
        }
        idamService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
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
        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));

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
        assertThat(userProfile.isEmailCommsConsent()).isEqualTo(false);
        assertThat(userProfile.isPostalCommsConsent()).isEqualTo(false);
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

    @After
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
