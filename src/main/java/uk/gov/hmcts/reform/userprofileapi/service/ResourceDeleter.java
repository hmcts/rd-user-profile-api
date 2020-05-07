package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

public interface ResourceDeleter<T extends RequestData> {

    UserProfilesDeletionResponse delete(T profileData);

}
