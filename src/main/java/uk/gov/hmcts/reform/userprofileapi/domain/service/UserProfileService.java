package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.RequestData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@Service
public class UserProfileService<T extends RequestData> {

    private ResourceCreator<T> resourceCreator;
    private ResourceRetriever<T> resourceRetriever;

    public UserProfileService(ResourceCreator<T> resourceCreator, ResourceRetriever<T> resourceRetriever) {
        this.resourceCreator = resourceCreator;
        this.resourceRetriever = resourceRetriever;
    }

    public UserProfileResource create(T requestData) {
        return new UserProfileResource(resourceCreator.create(requestData));
    }

    public UserProfileResource retrieve(T requestData) {
        return new UserProfileResource(resourceRetriever.retrieve(requestData));
    }

}
