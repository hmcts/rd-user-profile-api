package uk.gov.hmcts.reform.userprofileapi.infrastructure.repository;

import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceRetriever;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;

@Service
public class UserProfileRetriever implements ResourceRetriever {

    private UserProfileQuerySupplier querySupplier;

    public UserProfileRetriever(UserProfileQuerySupplier userProfileQuerySupplier) {
        this.querySupplier = userProfileQuerySupplier;
    }

    @Override
    public UserProfile retrieve(UserProfileIdentifier identifier) {

        return
            querySupplier.getRetrieveByIdQuery(identifier)
                .get()
                .orElseThrow(() ->
                    new DataRetrievalFailureException("Could not find resource from database with given identifier: " + identifier.getValue()));
    }

}
