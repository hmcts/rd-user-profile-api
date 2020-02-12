package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class RetrieveUserProfileWithIdamErrorsIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;

    @Before
    public void setUpWireMock() {

        idamService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                ));

        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(404)
                        ));

        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Iterable<UserProfile> userProfiles = testUserProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        UserProfile user1 = buildUserProfile();
        user1.setStatus(IdamStatus.ACTIVE);
        user1 = testUserProfileRepository.save(user1);


        assertTrue(testUserProfileRepository.existsById(user1.getId()));

        userProfileMap = new HashMap<>();
        userProfileMap.put("user", user1);
    }

    @Test
    public void should_fail_when_idam_returns_unsuccessfull_response_with_roles_by_id() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        NOT_FOUND
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        Optional<Audit> optional = auditRepository.findByUserProfile(userProfile);
        Audit audit = optional.get();

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(404);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.NOT_FOUND);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isNotNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

    @Test
    public void should_fail_when_idam_returns_unsuccessfull_response_resource_with_roles_by_email() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");
        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + "roles" + "?" + "email=" + userProfile.getEmail(),
                        NOT_FOUND
                );

        Optional<Audit> optional = auditRepository.findByUserProfile(userProfile);
        Audit audit = optional.get();

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(404);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.NOT_FOUND);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isNotNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

}
