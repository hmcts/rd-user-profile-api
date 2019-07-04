package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.IntTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.domain.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileWithDuplicateUserIntTest {

    private MockMvc mockMvc;

    private static final String APP_BASE_PATH = "/v1/userprofile";

    @Autowired
    protected UserProfileRepository userProfileRepository;

    @Autowired
    protected AuditRepository auditRepository;

    @Autowired
    protected IntTestRequestHandler intTestRequestHandler;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    @Rule
    public WireMockRule idamService = new WireMockRule(8888);

    private Map<String, UserProfile> userProfileMap;

    @Before
    public void setUpWireMock() {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        idamService.stubFor(WireMock.post(urlEqualTo("/user/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", UUID.randomUUID().toString())
                        .withStatus(409)
                ));
    }

    public void mockWithGetSuccess() {
        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"true\","
                                + "  \"forename\": \"fname\","
                                + "  \"surname\": \"lname\","
                                + "  \"email\": \"user@hmcts.net\","
                                + "  \"locked\": \"false\","
                                + "  \"roles\": ["
                                + "    \"pui-organisation-manager\","
                                + "    \"pui-case-manager\","
                                + "    \"pui-user-manager\""
                                + "  ]"
                                + "}")));

    }

    public void mockWithUpdateSuccess() {
        idamService.stubFor(put(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public void mockWithGetFail() {
        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(404)
                       ));

    }

    public void mockWithUpdateFail() {
        idamService.stubFor(put(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                ));
    }

    @Test
    public void should_return_201_and_create_user_profile_when_duplicate_in_sidam() throws Exception {

        mockWithGetSuccess();
        mockWithUpdateSuccess();
        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
            intTestRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                CREATED,
                CreateUserProfileResponse.class
            );

        verifyUserProfileCreation(createdResource, CREATED, data);

    }

    @Test
    public void should_return_404_and_not_create_user_profile_when_duplicate_in_sidam_and_get_failed() throws Exception {

        mockWithGetFail();
        mockWithUpdateSuccess();
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
                intTestRequestHandler.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        NOT_FOUND,
                        CreateUserProfileResponse.class
                );

        verifyUserProfileCreationForFailure(NOT_FOUND);

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_duplicate_in_sidam_and_update_failed() throws Exception {

        mockWithGetSuccess();
        mockWithUpdateFail();
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
                intTestRequestHandler.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST,
                        CreateUserProfileResponse.class
                );

        verifyUserProfileCreationForFailure(BAD_REQUEST);

    }

    private void verifyUserProfileCreation(CreateUserProfileResponse createdResource, HttpStatus idamStatus, CreateUserProfileData data) {

        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(UUID.class);
        assertThat(createdResource.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByIdamId(createdResource.getIdamId());
        UserProfile userProfile = persistedUserProfile.get();
        assertThat(userProfile.getId()).isNotNull().isExactlyInstanceOf(Long.class);
        assertThat(userProfile.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(userProfile.getEmail()).isEqualTo("user@hmcts.net");
        assertThat(userProfile.getFirstName()).isNotEmpty().isEqualTo("fname");
        assertThat(userProfile.getLastName()).isNotEmpty().isEqualTo("lname");
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
        Audit audit = optional.orElse(null);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.ACCEPTED);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.SIDAM);
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
        assertThat(audit.getSource()).isEqualTo(ResponseSource.SIDAM);
        assertThat(audit.getUserProfile()).isNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

}
