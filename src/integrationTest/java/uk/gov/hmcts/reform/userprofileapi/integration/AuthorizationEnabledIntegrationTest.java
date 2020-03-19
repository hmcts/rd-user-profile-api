package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRequestHandlerTest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.integration.util.TestUserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@Configuration
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990","IDAM_URL:http://127.0.0.1:5000"})
public class AuthorizationEnabledIntegrationTest {

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

    @Autowired
    protected TestUserProfileRepository testUserProfileRepository;

    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Rule
    public WireMockRule s2sService = new WireMockRule(8990);

    @Rule
    public WireMockRule idamService = new WireMockRule(5000);

    @Before
    public void setUpWireMock() {

        s2sService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("it")));

        idamService.stubFor(get(urlEqualTo("/details"))
                .withHeader("Authorization", equalTo("Bearer authorization-eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzdXBlci51c2VyQGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjZiYTdkYTk4LTRjMGYtNDVmNy04ZjFmLWU2N2NlYjllOGI1OCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI0NjAzYjVhYS00Y2ZhLTRhNDQtYWQzZC02ZWI0OTI2YjgxNzYiLCJhdWQiOiJteV9yZWZlcmVuY2VfZGF0YV9jbGllbnRfaWQiLCJuYmYiOjE1NTk4OTgxNzMsImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUiLCJzY29wZSI6WyJhY3IiLCJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsImF1dGhvcml0aWVzIl0sImF1dGhfdGltZSI6MTU1OTg5ODEzNTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NTk5MjY5NzMsImlhdCI6MTU1OTg5ODE3MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjgxN2ExNjE0LTVjNzAtNGY4YS05OTI3LWVlYjFlYzJmYWU4NiJ9.RLJyLEKldHeVhQEfSXHhfOpsD_b8dEBff7h0P4nZVLVNzVkNoiPdXYJwBTSUrXl4pyYJXEhdBwkInGp3OfWQKhHcp73_uE6ZXD0eIDZRvCn1Nvi9FZRyRMFQWl1l3Dkn2LxLMq8COh1w4lFfd08aj-VdXZa5xFqQefBeiG_xXBxWkJ-nZcW3tTXU0gUzarGY0xMsFTtyRRilpcup0XwVYhs79xytfbq0WklaMJ-DBTD0gux97KiWBrM8t6_5PUfMDBiMvxKfRNtwGD8gN8Vct9JUgVTj9DAIwg0KPPm1rEETRPszYI2wWvD2lpH2AwUtLBlRDANIkN9SdfiHSETvoQ"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{"
                                +  "  \"id\": \"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\","
                                +  "  \"forename\": \"Super\","
                                +  "  \"surname\": \"User\","
                                +  "  \"email\": \"super.user@hmcts.net\","
                                +  "  \"accountStatus\": \"active\","
                                +  "  \"roles\": ["
                                +  "  "
                                +  "  ]"
                                +  "}")));

        setSidamRegistrationMockWithStatus(201);

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
    }

    protected void setSidamRegistrationMockWithStatus(int status) {
        idamService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/v1/users/7f3c076c-e954-4d6f-80f7-6292160bf0bc")
                        .withStatus(status)
                ));
    }

    protected UserProfileDataResponse getMultipleUsers(UserProfileDataRequest request, HttpStatus expectedStatus, String showDeleted, String rolesRequired) throws Exception {
        return userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH + SLASH + "users?showdeleted=" + showDeleted + "&rolesRequired=" + rolesRequired,
                request,
                expectedStatus,
                UserProfileDataResponse.class
        );
    }

    protected Object createUser(UserProfileCreationData data, HttpStatus expectedStatus, Class clazz) throws Exception {
        return userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                expectedStatus,
                clazz
        );
    }

    protected void verifyUserProfileCreation(UserProfileCreationResponse createdResource, HttpStatus idamStatus, UserProfileCreationData data) {

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

        auditRepository.findAllByUserProfile(userProfile).forEach(audit -> verifyAudit(audit, createdResource));
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
    public void tearDown () {
        auditRepository.deleteAll();
        testUserProfileRepository.deleteAll();
    }
}
