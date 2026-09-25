package uk.gov.hmcts.reform.userprofileapi.integration.wiremock;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.util.LinkedHashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.integration.SpringBootIntegrationTest.getObjectMapper;

public final class IdamWireMockStubs {

    private IdamWireMockStubs() {
    }

    public static void registerDefaults(WireMockServer server) {

        server.stubFor(
                get(urlPathEqualTo("/o/userinfo"))
                        .atPriority(10)
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                        .withBody(getUserDetailsJson())
                                        .withTransformers("location_token_response")
                        )
        );
    }

    private static String getUserDetailsJson() {
        try {
            LinkedHashMap<String, Object> data = new LinkedHashMap<>();
            data.put("id", "%s");
            data.put("uid", "%s");
            data.put("forename", "Super");
            data.put("surname", "User");
            data.put("email", "dummy@email.com");
            data.put("roles", List.of("%s"));

            return getObjectMapper().writeValueAsString(data);

        } catch (Exception e) {
            throw new IllegalStateException("Unable to create IDAM userinfo response", e);
        }
    }
}