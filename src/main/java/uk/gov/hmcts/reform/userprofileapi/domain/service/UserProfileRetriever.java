package uk.gov.hmcts.reform.userprofileapi.domain.service;

import java.util.UUID;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileRetriever implements ResourceRetriever {

    private UserProfileRepository userProfileRepository;

    public UserProfileRetriever(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfileResource retrieve(String id) {

        UUID uuid = UUID.fromString(id);

        UserProfile userProfile = userProfileRepository.findById(uuid)
            .orElseThrow(() -> new DataRetrievalFailureException("Could not find resource from database with id: " + id));

        return new UserProfileResource(userProfile);

    }

}
