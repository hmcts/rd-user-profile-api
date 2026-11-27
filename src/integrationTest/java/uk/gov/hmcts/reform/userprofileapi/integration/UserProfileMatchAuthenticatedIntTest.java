package uk.gov.hmcts.reform.userprofileapi.integration;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.userprofileapi.integration.util.JwtTokenUtil.generateAuthToken;
import static uk.gov.hmcts.reform.userprofileapi.integration.wiremock.WireMockTestEnvironment.oidcIssuer;

class UserProfileMatchAuthenticatedIntTest extends AuthorizationEnabledIntegrationTest {
    @Autowired
    @Qualifier("idamMockServer")
    private WireMockServer idam;
    @Autowired
    @Qualifier("s2sMockServer")
    private WireMockServer s2s;

    @Test
    void authorisesARealSignedRequestAndMatchesTheStoredProfile() throws Exception {
        String bearer = "Bearer " + generateAuthToken(oidcIssuer(), false);
        String service = "Bearer match-integration-prd";
        s2s.stubFor(get(urlEqualTo("/details")).withHeader("Authorization", equalTo(service))
                .atPriority(1).willReturn(ok("rd_professional_api")));
        idam.stubFor(get(urlEqualTo("/o/userinfo")).withHeader("Authorization", equalTo(bearer)).atPriority(1)
                .willReturn(okJson("{\"roles\":[\"prd-admin\"],\"sub\":\"synthetic-admin\"}")));
        var profile = buildUserProfile();
        profile.setStatus(IdamStatus.ACTIVE);
        userProfileRepository.save(profile);
        try (var client = HttpClient.newHttpClient()) {
            var request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port
                    + "/v1/userprofile/users/match"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", bearer)
                    .header("ServiceAuthorization", service)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(getObjectMapper().writeValueAsString(Map.of(
                            "userIds", List.of(profile.getIdamId()), "fullNameContains", profile.getFirstName()))))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertThat(response.statusCode()).isEqualTo(200);
            var match = getObjectMapper().readTree(response.body());
            assertThat(match.get("unavailableUserIds")).isEqualTo(getObjectMapper().createArrayNode());
            assertThat(match.get("matchedProfiles")).isEqualTo(getObjectMapper().valueToTree(List.of(Map.of(
                    "userIdentifier", profile.getIdamId(), "firstName", profile.getFirstName(),
                    "lastName", profile.getLastName(), "email", profile.getEmail()))));
        }
    }
}
