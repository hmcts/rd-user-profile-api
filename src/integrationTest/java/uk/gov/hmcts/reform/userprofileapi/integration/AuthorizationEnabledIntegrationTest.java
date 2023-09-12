package uk.gov.hmcts.reform.userprofileapi.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.lib.util.serenity5.SerenityTest;
import uk.gov.hmcts.reform.userprofileapi.ProfileConfig;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRequestHandlerTest;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SerenityTest
@ExtendWith(SpringExtension.class)
@WithTags({@WithTag("testType:Integration")})
@SpringBootTest(webEnvironment = RANDOM_PORT)
@Configuration
@Import(ProfileConfig.class)
@TestPropertySource(properties = {"S2S_URL=http://127.0.0.1:8990", "IDAM_URL:http://127.0.0.1:5000"})
@DirtiesContext
public abstract class AuthorizationEnabledIntegrationTest extends SpringBootIntegrationTest {

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

    @Value("${idam.s2s-auth.microservice}")
    static String authorisedService;

    @MockBean
    protected JwtDecoder jwtDecoder;

    @BeforeEach
    public void setUpWireMock() throws JsonProcessingException {

        s2sMockService.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_user_profile_api")));


        setSidamRegistrationMockWithStatus(HttpStatus.CREATED.value(), true);

        HashMap<String,String> data = new HashMap<>();
        data.put("active","true");
        data.put("forename","Super");
        data.put("surname","User");
        data.put("email","test@test.com");
        data.put("pending","false");
        data.put("roles","[pui-organisation-manager]");

        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsString(data))));

        UserInfo userDetails = UserInfo.builder()
            .givenName("Suspended")
            .familyName("User")
            .roles(List.of("pui-organisation-manager"))
            .sub("false")
            .build();

        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(objectMapper.writeValueAsString(userDetails))));

        idamMockService.stubFor(delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(204)
                        .withBody("{"
                                + "  \"response\": \"User deleted successfully.\""
                                + "}")));
        UserInfo userDetailsNew = UserInfo.builder()
            .uid("%s")
            .givenName("User")
            .familyName("User")
            .name("Super")
            .roles(List.of("pui-organisation-manager"))
            .sub("active")
            .build();
        idamMockService.stubFor(get(urlEqualTo("/o/userinfo"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(userDetailsNew))));

        when(featureToggleService.isFlagEnabled(anyString(), anyString())).thenReturn(true);

    }

    public static String generateDummyS2SToken(String serviceName) {
        return Jwts.builder()
            .setSubject(serviceName)
            .setIssuedAt(new Date())
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.encode("AA"))
            .compact();
    }

    public static synchronized Jwt getJwt() {
        var s2SToken = generateDummyS2SToken(authorisedService);
        return Jwt.withTokenValue(s2SToken)
            .claim("exp", Instant.ofEpochSecond(1585763216))
            .claim("iat", Instant.ofEpochSecond(1585734416))
            .claim("token_type", "Bearer")
            .claim("tokenName", "access_token")
            .claim("expires_in", 28800)
            .header("kid", "b/O6OvVv1+y+WgrH5Ui9WTioLt0=")
            .header("typ", "RS256")
            .header("alg", "RS256")
            .build();
    }

    public void searchUserProfileSyncWireMock(HttpStatus status, String id) throws JsonProcessingException {

        String body = null;
        int returnHttpStaus = status.value();
        if (status.is2xxSuccessful() && StringUtils.isNotBlank(id)) {
            body = "[{"
                    + "  \"id\": \""
                    + id
                    + "\" ,"
                    + "  \"forename\": \"Super\","
                    + "  \"surname\": \"User\","
                    + "  \"email\": \"dummy@email.com\","
                    + "  \"active\": \"true\","
                    + "  \"roles\": ["
                    + "  \"pui-case-manager\""
                    + "  ]"
                    + "}]";
            returnHttpStaus = 200;
        } else if (status.is2xxSuccessful() && StringUtils.isBlank(id)) {
            body = "[{"
                    + "  \"id\": \"ef4fac86-d3e8-47b6-88a7-c7477fb69d3f\","
                    + "  \"forename\": \"Super\","
                    + "  \"surname\": \"User\","
                    + "  \"email\": \"dummy@email.com\","
                    + "  \"active\": \"true\","
                    + "  \"roles\": ["
                    + "  \"pui-case-manager\""
                    + "  ]"
                    + "}]";
            returnHttpStaus = 200;

        } else if (status.is4xxClientError()) {
            returnHttpStaus = 400;
        }

        idamMockService.stubFor(get(urlPathMatching("/api/v1/users"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("X-Total-Count", "1")
                        .withBody(body)
                        .withStatus(returnHttpStaus)));

    }

    protected void setSidamUserUpdateMockWithStatus(int status, boolean setBodyEmpty, String idamId)
        throws JsonProcessingException {
        String body = null;
        if (status == 404 && !setBodyEmpty) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(404)
                .errorMessage("Not Found")
                .build();
            body = objectMapper.writeValueAsString(errorResponse);
        }
        idamMockService.stubFor(patch(urlMatching("/api/v1/users/" + idamId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(status)
                        .withBody(body)
                ));
    }

    protected void setSidamRegistrationMockWithStatus(int status, boolean setBodyEmpty)  {
        String body = null;
        ErrorResponse errorResponse;
        if (status == 400 && !setBodyEmpty) {
            errorResponse = ErrorResponse.builder()
                .status(400)
                .errorMessage("Role to be assigned does not exist.")
                .build();
            try {
                body = objectMapper.writeValueAsString(errorResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }

        } else if (status == 409 && !setBodyEmpty) {
            errorResponse = ErrorResponse.builder()
                .status(409)
                .errorMessage("[A user is already registered with this email.]")
                .build();
            try {
                body = objectMapper.writeValueAsString(errorResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        } else if (status == 404 && !setBodyEmpty) {
            errorResponse = ErrorResponse.builder()
                .status(404)
                .errorMessage("16 Resource not found")
                .errorDescription("The role to be assigned does not exist.")
                .build();
            try {
                body = objectMapper.writeValueAsString(errorResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
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
            ErrorResponse errorResponse = ErrorResponse.builder()
                .status(404)
                .errorMessage("The user could not be found: c5d631f-af11-4816-abbe-ac6fd9b99ee9")
                .build();
            try {
                body = objectMapper.writeValueAsString(errorResponse);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }
        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));

    }

    public void healthEndpointMock() throws JsonProcessingException {

        HashMap<String,String> data = new HashMap<>();
        data.put("status","UP");
        s2sMockService.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(data))));
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
