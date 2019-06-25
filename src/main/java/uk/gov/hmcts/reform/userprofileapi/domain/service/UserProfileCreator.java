package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileCreator implements ResourceCreator<CreateUserProfileData> {

    private IdamService idamService;
    private UserProfileRepository userProfileRepository;

    public UserProfileCreator(UserProfileRepository userProfileRepository, IdamService idamService) {
        this.userProfileRepository = userProfileRepository;
        this.idamService = idamService;
    }

    public UserProfile create(CreateUserProfileData profileData) {

        // Call idam to register new user and store response
        final IdamRegistrationInfo idamRegistrationInfo = idamService.registerUser(profileData);

        UserProfile userProfile = new UserProfile(profileData, idamRegistrationInfo);

        return userProfileRepository.save(userProfile);
    }

}
