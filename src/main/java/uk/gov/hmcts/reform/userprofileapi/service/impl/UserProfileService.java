package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceCreator;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceRetriever;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;

@Service
public class UserProfileService<T extends RequestData> {

    @Autowired
    private ResourceCreator<T> resourceCreator;

    @Autowired
    private ResourceRetriever<T> resourceRetriever;

    @Autowired
    private ResourceUpdator<T> resourceUpdator;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfileCreationResponse create(T requestData) {
        return new UserProfileCreationResponse(resourceCreator.create(requestData));
    }

    public UserProfileWithRolesResponse retrieveWithRoles(T requestData) {
        return new UserProfileWithRolesResponse(resourceRetriever.retrieve(requestData, true), true);
    }

    public UserProfileDataResponse retrieveWithRoles(T requestData, boolean showDeleted, boolean rolesRequired) {
        return new UserProfileDataResponse(resourceRetriever.retrieveMultipleProfiles(requestData, showDeleted, rolesRequired), rolesRequired);
    }

    public UserProfileResponse retrieve(T requestData) {
        return new UserProfileResponse(resourceRetriever.retrieve(requestData, false));
    }

    public UserProfileResponse update(T updateData, String userId, ResponseSource origin) {
        return resourceUpdator.update(updateData, userId, origin).orElse(new UserProfileResponse());
    }

    public UserProfileResponse updateRoles(T updateData, String userId) {
        return  resourceUpdator.updateRoles(updateData, userId);
    }
}
