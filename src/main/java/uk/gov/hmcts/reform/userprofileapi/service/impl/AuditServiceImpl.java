package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;

@Service
public class AuditServiceImpl implements AuditService {

    @Override
    public void persistAudit(HttpStatus idamStatus, UserProfile userProfile, ResponseSource responseSource) {

    }

    @Override
    public void persistAudit(HttpStatus idamStatus, ResponseSource responseSource) {

    }
}
