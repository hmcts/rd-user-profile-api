package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

public interface ResourceUpdator<T extends RequestData> {

    Optional<UserProfileResponse> update(T profileData, String userId, ResponseSource origin);

    UserProfileResponse updateRoles(T profileData, String userId);

}
