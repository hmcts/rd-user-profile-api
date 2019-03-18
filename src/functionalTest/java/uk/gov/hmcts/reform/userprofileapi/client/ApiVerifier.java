package uk.gov.hmcts.reform.userprofileapi.client;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;

public interface ApiVerifier<P extends RequestData, R> {

    R verifyPost(P data, String path);

    void verifyPost(String data, HttpStatus status, String path);

    R verifyGet(R expectation, String path);

    void verifyGet(HttpStatus status, String path);
}
