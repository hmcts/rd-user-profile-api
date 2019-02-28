package uk.gov.hmcts.reform.userprofileapi.infrastructure.security;

import java.util.Optional;

public interface AccessTokenProvider {

    String getAccessToken();

    Optional<String> tryGetAccessToken();
}
