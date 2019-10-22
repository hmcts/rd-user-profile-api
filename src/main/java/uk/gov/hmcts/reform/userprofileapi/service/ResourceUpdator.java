package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ResourceUpdator<T extends RequestData> {

    Optional<UserProfile> update(T profileData, String userId);

    AttributeResponse update(T profileData, String userId, String origin);

    UserProfileRolesResponse updateRoles(T profileData, String userId);

}
