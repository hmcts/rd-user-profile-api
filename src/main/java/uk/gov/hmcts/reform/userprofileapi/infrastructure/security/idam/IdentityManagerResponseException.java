package uk.gov.hmcts.reform.userprofileapi.infrastructure.security.idam;

import uk.gov.hmcts.reform.logging.exception.AlertLevel;
import uk.gov.hmcts.reform.logging.exception.UnknownErrorCodeException;

@SuppressWarnings("squid:MaximumInheritanceDepth")
public class IdentityManagerResponseException extends UnknownErrorCodeException {

    public IdentityManagerResponseException(
        String message,
        Throwable cause) {

        super(AlertLevel.P2, message, cause);

    }

}
