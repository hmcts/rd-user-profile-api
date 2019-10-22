package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface AuditService {

    void persistAudit(HttpStatus idamStatus, UserProfile userProfile, ResponseSource responseSource);

    void persistAudit(HttpStatus idamStatus, ResponseSource responseSource);

}
