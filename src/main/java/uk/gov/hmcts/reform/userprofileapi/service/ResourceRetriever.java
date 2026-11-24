package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfileIdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;

import java.util.List;

public interface ResourceRetriever<T extends RequestData> {

    UserProfile retrieve(T identifier, boolean fetchRoles, String origin);

    List<UserProfile> retrieveMultipleProfiles(T identifier, boolean showDeleted, boolean rolesRequired);

    List<UserProfileIdamStatus> retrieveMultipleProfilesByCategory(String category);

}
