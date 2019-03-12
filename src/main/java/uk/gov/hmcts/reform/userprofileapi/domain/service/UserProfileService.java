package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRetriever;

@Service
public class UserProfileService {

    private UserProfileCreator userProfileCreator;
    private UserProfileRetriever userProfileRetriever;

    public UserProfileService(UserProfileCreator userProfileCreator, UserProfileRetriever userProfileRetriever) {
        this.userProfileCreator = userProfileCreator;
        this.userProfileRetriever = userProfileRetriever;
    }

    public UserProfileResource handleCreate(CreateUserProfileData requestData) {
        return new UserProfileResource(userProfileCreator.create(requestData));
    }

    public UserProfileResource handleRetrieve(UserProfileIdentifier requestData) {
        return new UserProfileResource(userProfileRetriever.retrieve(requestData));
    }

}
