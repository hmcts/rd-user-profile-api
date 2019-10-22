package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ResourceUpdator<T extends RequestData> {

    Optional<UserProfile> update(T profileData, String userId);

    UserProfileRolesResponse updateRoles(T profileData, String userId);

}
