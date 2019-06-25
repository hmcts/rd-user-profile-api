package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Before;
import org.junit.Rule;

public class AbstractIntegration {

    @Rule
    public WireMockRule idamService = new WireMockRule(8888);



    @Before
    public void setUpWireMock() {

        idamService.stubFor(post(urlEqualTo("/user/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(201)
                ));

        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"true\","
                                + "  \"forename\": \"Super\","
                                + "  \"surname\": \"User\","
                                + "  \"email\": \"super.user@hmcts.net\","
                                + "  \"locked\": \"false\","
                                + "  \"roles\": ["
                                + "    \"pui-organisation-manager\""
                                + "  ]"
                                + "}")));
    }
}
