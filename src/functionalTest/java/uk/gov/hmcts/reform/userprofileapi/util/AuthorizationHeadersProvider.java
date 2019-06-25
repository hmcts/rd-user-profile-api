package uk.gov.hmcts.reform.userprofileapi.util;

import io.restassured.http.Header;
import io.restassured.http.Headers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;


@Service
public class AuthorizationHeadersProvider {

    @Autowired
    private AuthTokenGenerator serviceAuthTokenGenerator;

    public Headers getServiceAuthorization() {

        String serviceToken = serviceAuthTokenGenerator.generate();

        return new Headers(
            new Header("ServiceAuthorization", serviceToken)
        );
    }
}
