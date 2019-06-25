package uk.gov.hmcts.reform.userprofileapi.util;

import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;

public interface UserProfileValidator {

    static void isUserIdValid(String userId) {
        try {
            java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Malformed userId. Should have UUID format");
        }
    }
}
