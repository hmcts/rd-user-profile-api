package uk.gov.hmcts.reform.userprofileapi.integration.wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.HashMap;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static uk.gov.hmcts.reform.userprofileapi.integration.SpringBootIntegrationTest.getObjectMapper;

public class S2sWireMockStubs {

    private static WireMockServer s2sMockServer = null;

    private S2sWireMockStubs() {
    }

    public static void registerDefaults(WireMockServer server) {
        s2sMockServer = server;
        server.stubFor(get(urlEqualTo("/details"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("rd_user_profile_api")));
    }

    public static void healthEndpointMock() throws JsonProcessingException {
        HashMap<String, String> data = new HashMap<>();
        data.put("status", "UP");
        s2sMockServer.stubFor(get(urlEqualTo("/health"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(getObjectMapper().writeValueAsString(data))));
    }
}