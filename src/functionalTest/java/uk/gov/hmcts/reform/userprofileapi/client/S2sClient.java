package uk.gov.hmcts.reform.userprofileapi.client;

import com.google.common.collect.ImmutableMap;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
public class S2sClient {

    private final String s2sUrl;
    private final String microserviceName;
    private final String microserviceKey;
    private final GoogleAuthenticator authenticator = new GoogleAuthenticator();

    public S2sClient(String s2sUrl, String microserviceName, String microserviceKey) {
        this.s2sUrl = s2sUrl;
        this.microserviceName = microserviceName;
        try {
            log.info("Configured S2S secret: ".concat(microserviceKey.substring(0, 2)).concat("************")
                    .concat(microserviceKey.substring(14)));
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        this.microserviceKey = microserviceKey;
    }

    /**
     * Sign in to s2s.
     *
     * @return s2s JWT token.
     */
    public String getS2sToken() {
        Map<String, Object> params = ImmutableMap.of("microservice",
                this.microserviceName,
                "oneTimePassword",
                authenticator.getTotpPassword(this.microserviceKey));

        Response response = SerenityRest
                .given()
                .relaxedHTTPSValidation()
                .baseUri(this.s2sUrl)
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .body(params)
                .post("/lease")
                .andReturn();

        assertThat(response.getStatusCode()).isEqualTo(200);

        String jwtToken = response.getBody().asString();
        log.debug("Got JWT from S2S service: ", jwtToken);
        return jwtToken;
    }
}

