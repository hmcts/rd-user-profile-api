package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileQueryProvider;

@Service
public class UserProfileRetriever implements ResourceRetriever<UserProfileIdentifier> {

    private UserProfileQueryProvider querySupplier;

    public UserProfileRetriever(UserProfileQueryProvider userProfileQueryProvider) {
        this.querySupplier = userProfileQueryProvider;
    }

    @Override
    public UserProfile retrieve(UserProfileIdentifier identifier) {

        return
            querySupplier.getRetrieveByIdQuery(identifier)
                .get()
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Could not find resource from database with given identifier: "
                        + identifier.getValue()));
    }

}
