package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

public interface ResourceCreator<T extends RequestData> {

    UserProfile create(T profileData);

    UserProfile reInviteUser(T profileData);

}
