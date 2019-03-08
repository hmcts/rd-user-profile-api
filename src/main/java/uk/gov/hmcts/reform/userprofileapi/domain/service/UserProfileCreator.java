package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileCreator {

    private UserProfileRepository userProfileRepository;

    public UserProfileCreator(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfileResource create(UserProfileCreationData profileData) {

        //tasks to perform
        //Check if profile already exists by e-mail?
        //1: call idam to register new user
        //2: create db row for new user
        //Respond with required info including new idam-id and new profile UUID

        String idamId = "idamId";
        UserProfile userProfile =
            new UserProfile(
                idamId,
                profileData.getEmail(),
                profileData.getFirstName(),
                profileData.getLastName());

        return new UserProfileResource(userProfileRepository.save(userProfile));
    }
}
