package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

public interface DeleteResourceService<T extends RequestData> {

    UserProfilesDeletionResponse delete(T profileData);

}
