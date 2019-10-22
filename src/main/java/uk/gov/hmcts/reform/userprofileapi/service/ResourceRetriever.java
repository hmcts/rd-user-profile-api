package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.List;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ResourceRetriever<T extends RequestData> {

    UserProfile retrieve(T identifier, boolean fetchRoles);

    List<UserProfile> retrieveMultipleProfiles(T identifier, boolean showDeleted, boolean rolesRequired);

}
