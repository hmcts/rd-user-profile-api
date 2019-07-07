package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ResourceUpdator<T extends RequestData> {

    UserProfile update(T profileData, ResourceRetriever resourceRetriever, String userId);

}
