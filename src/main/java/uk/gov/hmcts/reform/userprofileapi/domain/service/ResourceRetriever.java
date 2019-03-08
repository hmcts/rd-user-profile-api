package uk.gov.hmcts.reform.userprofileapi.domain.service;

import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

public interface ResourceRetriever {

    UserProfileResource retrieve(String id);

}
