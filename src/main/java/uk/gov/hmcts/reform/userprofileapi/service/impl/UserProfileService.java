package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.service.DeleteResourceService;
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
    private DeleteResourceService<T> resourceDeleter;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public UserProfileCreationResponse create(T requestData) {
        return new UserProfileCreationResponse(resourceCreator.create(requestData));
    }

    public UserProfileCreationResponse reInviteUser(T requestData) {
        return new UserProfileCreationResponse(resourceCreator.reInviteUser(requestData));
    }

    public UserProfileWithRolesResponse retrieveWithRoles(T requestData) {
        return new UserProfileWithRolesResponse(resourceRetriever.retrieve(requestData, true),
                true);
    }

    public UserProfileDataResponse retrieveWithRoles(T requestData, boolean showDeleted, boolean rolesRequired) {
        return new UserProfileDataResponse(resourceRetriever.retrieveMultipleProfiles(requestData, showDeleted,
                rolesRequired), rolesRequired);
    }

    public UserProfileResponse retrieve(T requestData) {
        return new UserProfileResponse(resourceRetriever.retrieve(requestData, false));
    }

    public AttributeResponse update(T updateData, String userId, String origin) {
        return resourceUpdator.update(updateData, userId, origin);
    }

    public UserProfileRolesResponse updateRoles(T updateData, String userId) {
        return  resourceUpdator.updateRoles(updateData, userId);
    }

    public UserProfilesDeletionResponse delete(T requestData) {
        return resourceDeleter.delete(requestData);
    }
}
