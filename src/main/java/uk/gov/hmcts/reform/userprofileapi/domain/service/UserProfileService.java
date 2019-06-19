package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;

@Service
public class UserProfileService<T extends RequestData> {

    private ResourceCreator<T> resourceCreator;
    private ResourceRetriever<T> resourceRetriever;

    public UserProfileService(ResourceCreator<T> resourceCreator, ResourceRetriever<T> resourceRetriever) {
        this.resourceCreator = resourceCreator;
        this.resourceRetriever = resourceRetriever;
    }

    public CreateUserProfileResponse create(T requestData) {
        return new CreateUserProfileResponse(resourceCreator.create(requestData));
    }

    public GetUserProfileWithRolesResponse retrieve(T requestData) {
        return new GetUserProfileWithRolesResponse(resourceRetriever.retrieve(requestData));
    }

}
