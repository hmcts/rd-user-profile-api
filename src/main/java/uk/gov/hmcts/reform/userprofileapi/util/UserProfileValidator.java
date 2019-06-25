package uk.gov.hmcts.reform.userprofileapi.util;

import org.apache.commons.lang.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;

public interface UserProfileValidator {

    static void isUserIdValid(String userId) {
        if (StringUtils.isBlank(userId)) {
            throw new ResourceNotFoundException("userId is null or blank. Should have UUID format");
        }

        try {
            java.util.UUID.fromString(userId);
        } catch (IllegalArgumentException ex) {
            throw new ResourceNotFoundException("Malformed userId. Should have UUID format");
        }
    }
}
