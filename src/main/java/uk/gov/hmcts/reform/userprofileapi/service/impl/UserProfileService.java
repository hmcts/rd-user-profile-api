package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.clients.RequestData;
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

    public CreateUserProfileResponse create(T requestData) {
        return new CreateUserProfileResponse(resourceCreator.create(requestData));
    }

    public GetUserProfileWithRolesResponse retrieveWithRoles(T requestData) {
        return new GetUserProfileWithRolesResponse(resourceRetriever.retrieve(requestData, true));
    }

    public GetUserProfileResponse retrieve(T requestData) {
        return new GetUserProfileResponse(resourceRetriever.retrieve(requestData, false));
    }

    public void update(T updateData, String userId) {
        resourceUpdator.update(updateData, resourceRetriever, userId);
    }

}
