package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam.IdamService;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileQueryProvider;

@Service
public class UserProfileRetriever implements ResourceRetriever<UserProfileIdentifier> {

    private UserProfileQueryProvider querySupplier;
    private IdamService idamService;

    public UserProfileRetriever(UserProfileQueryProvider userProfileQueryProvider, IdamService idamService) {
        this.querySupplier = userProfileQueryProvider;
        this.idamService = idamService;
    }

    @Override
    public UserProfile retrieve(UserProfileIdentifier identifier) {

        UserProfile userProfile =
            querySupplier.getRetrieveByIdQuery(identifier)
                .get()
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Could not find resource from database with given identifier: "
                        + identifier.getValue()));
        IdamRolesInfo idamRolesInfo = idamService.getIdamRoles();
        userProfile.setRoles(idamRolesInfo);
        return userProfile;
    }

}
