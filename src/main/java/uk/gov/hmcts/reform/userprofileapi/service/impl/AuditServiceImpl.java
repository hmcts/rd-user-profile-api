package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditRepository auditRepository;

    @Override
    public void persistAudit(HttpStatus idamStatus, UserProfile userProfile, ResponseSource responseSource) {
        Audit audit = new Audit(idamStatus.value(), resolveStatusAndReturnMessage(idamStatus), responseSource,
                userProfile);
        auditRepository.save(audit);
    }

    @Override
    public void persistAudit(HttpStatus idamStatus, ResponseSource responseSource) {
        Audit audit = new Audit(idamStatus.value(), resolveStatusAndReturnMessage(idamStatus), responseSource);
        auditRepository.save(audit);
    }

    @Override
    public void persistAudit(IdamRolesInfo idamRolesInfo, UserProfile userProfile) {
        Audit audit = new Audit(idamRolesInfo.getResponseStatusCode().value(), idamRolesInfo.getStatusMessage(),
                ResponseSource.API, userProfile);
        auditRepository.save(audit);
    }

    @Override
    public void persistAudit(UserProfilesDeletionResponse userProfilesDeletionResponse) {
        Audit audit = new Audit(userProfilesDeletionResponse.getStatusCode(),
                userProfilesDeletionResponse.getMessage(), ResponseSource.API);
        auditRepository.save(audit);
    }

}
