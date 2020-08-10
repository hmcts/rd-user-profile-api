package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;

public interface AuditService {

    void persistAudit(HttpStatus idamStatus, UserProfile userProfile, ResponseSource responseSource);

    void persistAudit(HttpStatus idamStatus, ResponseSource responseSource);

    void persistAudit(IdamRolesInfo idamRolesInfo, UserProfile userProfile);

    void persistAudit(UserProfilesDeletionResponse userProfilesDeletionResponse);

}
