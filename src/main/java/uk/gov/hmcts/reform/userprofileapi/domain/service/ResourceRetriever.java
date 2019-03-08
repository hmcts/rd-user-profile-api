package uk.gov.hmcts.reform.userprofileapi.domain.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;

public interface ResourceRetriever {

    UserProfile retrieve(UserProfileIdentifier identifier);

}
