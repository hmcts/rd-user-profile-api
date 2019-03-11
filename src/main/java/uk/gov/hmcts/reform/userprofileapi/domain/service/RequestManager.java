package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRetriever;

@Service
public class RequestManager {

    private UserProfileCreator userProfileCreator;
    private UserProfileRetriever userProfileRetriever;

    public RequestManager(UserProfileCreator userProfileCreator, UserProfileRetriever userProfileRetriever) {
        this.userProfileCreator = userProfileCreator;
        this.userProfileRetriever = userProfileRetriever;
    }

    public UserProfileResource handle(CreateUserProfileData requestData) {
        return new UserProfileResource(userProfileCreator.create(requestData));
    }

    public UserProfileResource handle(UserProfileIdentifier requestData) {
        return new UserProfileResource(userProfileRetriever.retrieve(requestData));
    }

}
