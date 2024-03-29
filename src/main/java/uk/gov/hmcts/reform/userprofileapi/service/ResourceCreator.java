package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

public interface ResourceCreator<T extends RequestData> {

    UserProfile create(T profileData, String origin);

    UserProfile reInviteUser(T profileData, String origin);

}
