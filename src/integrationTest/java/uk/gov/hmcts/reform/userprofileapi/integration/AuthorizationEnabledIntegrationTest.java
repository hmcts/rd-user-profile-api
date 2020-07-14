package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
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
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990","IDAM_URL:http://127.0.0.1:5000"})
@DirtiesContext
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


        setSidamRegistrationMockWithStatus(HttpStatus.CREATED.value());

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
        return audits.stream().filter(audit -> audit.getUserProfile().getIdamId().equalsIgnoreCase(idamId)).collect(Collectors.toList());
    }
}
