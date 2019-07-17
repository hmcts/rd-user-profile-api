package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import java.util.List;

public interface ResourceRetriever<T extends RequestData> {

    UserProfile retrieve(T identifier, boolean fetchRoles);
    List<UserProfile> retrieveMultipleProfiles(T identifier, boolean showDeleted);

}
