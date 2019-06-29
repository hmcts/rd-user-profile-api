package uk.gov.hmcts.reform.userprofileapi.domain.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;

public interface ResourceUpdator<T extends RequestData> {

    UserProfile update(T profileData, ResourceRetriever resourceRetriever, String userId);

}
