package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@Service
public class RequestManager {

    private UserProfileCreator userProfileCreator;
    private UserProfileRetriever userProfileRetriever;

    public RequestManager(UserProfileCreator userProfileCreator, UserProfileRetriever userProfileRetriever) {
        this.userProfileCreator = userProfileCreator;
        this.userProfileRetriever = userProfileRetriever;
    }

    public UserProfileResource handle(UserProfileCreationData requestData) {
        return userProfileCreator.create(requestData);
    }

    public UserProfileResource handle(UserProfileIdentifier requestData) {
        return userProfileRetriever.retrieve(requestData.getIdentifier().getValue());
    }

}
