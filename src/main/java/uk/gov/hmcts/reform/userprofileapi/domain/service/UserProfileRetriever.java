package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileQueryProvider;

@Service
public class UserProfileRetriever implements ResourceRetriever<UserProfileIdentifier> {

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
            IdamRolesInfo idamRolesInfo = idamService.getUserById(userProfile.getIdamId().toString());
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
