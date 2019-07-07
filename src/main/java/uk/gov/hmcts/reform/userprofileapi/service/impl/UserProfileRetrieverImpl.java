package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.controllers.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.controllers.advice.IdamServiceException;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceRetriever;

@Service
public class UserProfileRetrieverImpl implements ResourceRetriever<UserProfileIdentifier> {

    @Autowired
    private UserProfileQueryProvider querySupplier;
    @Autowired
    private IdamService idamService;
    @Autowired
    private AuditRepository auditRepository;

    @Override
    public UserProfile retrieve(UserProfileIdentifier identifier, boolean fetchRoles) {

        UserProfile userProfile =
            querySupplier.getRetrieveByIdQuery(identifier)
                .get()
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Could not find resource from database with given identifier: "
                        + identifier.getValue()));
        if (fetchRoles) {
            IdamRolesInfo idamRolesInfo = idamService.getUserById(userProfile);
            if (idamRolesInfo.getIdamGetResponseStatusCode().is2xxSuccessful()) {
                persistAudit(idamRolesInfo, userProfile);
                userProfile.setRoles(idamRolesInfo);
            } else {
                persistAudit(idamRolesInfo, userProfile);
                throw new IdamServiceException(idamRolesInfo.getStatusMessage(), idamRolesInfo.getIdamGetResponseStatusCode());
            }
        }
        return userProfile;
    }

    private void persistAudit(IdamRolesInfo idamRolesInfo, UserProfile userProfile) {
        Audit audit = new Audit(idamRolesInfo.getIdamGetResponseStatusCode().value(), idamRolesInfo.getStatusMessage(), ResponseSource.SIDAM, userProfile);
        auditRepository.save(audit);
    }

}
