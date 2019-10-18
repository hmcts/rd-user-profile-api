package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.client.AttributeResponse;
import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;

public interface ResourceUpdator<T extends RequestData> {

    AttributeResponse update(T profileData, String userId, String origin);

    UserProfileRolesResponse updateRoles(T profileData, String userId);

}
