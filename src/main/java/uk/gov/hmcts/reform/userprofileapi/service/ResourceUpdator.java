package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ResourceUpdator<T extends RequestData> {

    UserProfile update(T profileData, String userId);

    void addRoles(T profileData, String userId, String addOrDelete);

}
