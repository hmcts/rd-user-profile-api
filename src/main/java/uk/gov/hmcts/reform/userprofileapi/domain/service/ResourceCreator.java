package uk.gov.hmcts.reform.userprofileapi.domain.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;

public interface ResourceCreator<T extends RequestData> {

    UserProfile create(T profileData);

}
