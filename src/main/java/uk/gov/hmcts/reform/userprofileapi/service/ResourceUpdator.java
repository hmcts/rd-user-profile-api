package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

public interface ResourceUpdator<T extends RequestData> {

    AttributeResponse update(T profileData, String userId, String origin);

    UserProfileRolesResponse updateRoles(T profileData, String userId);

}
