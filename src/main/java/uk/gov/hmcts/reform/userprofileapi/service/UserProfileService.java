package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.RequestData;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;

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

    public CreateUserProfileResponse create(T requestData) {
        return new CreateUserProfileResponse(resourceCreator.create(requestData));
    }

    public GetUserProfileWithRolesResponse retrieveWithRoles(T requestData) {
        return new GetUserProfileWithRolesResponse(resourceRetriever.retrieve(requestData, true));
    }

    public GetUserProfilesResponse retrieveWithRoles(T requestData, boolean showDeleted) {
        return new GetUserProfilesResponse(resourceRetriever.retrieveMultipleProfiles(requestData, showDeleted));
    }

    public GetUserProfileResponse retrieve(T requestData) {
        return new GetUserProfileResponse(resourceRetriever.retrieve(requestData, false));
    }

    public void update(T updateData, String userId) {
        resourceUpdator.update(updateData, userId);
    }

}
