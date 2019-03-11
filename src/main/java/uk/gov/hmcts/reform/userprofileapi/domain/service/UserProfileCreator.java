package uk.gov.hmcts.reform.userprofileapi.domain.service;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileCreator {

    private UserProfileRepository userProfileRepository;

    //TODO introduce IdamService to register user
    //private IdentityManagerService identityManagerService;

    public UserProfileCreator(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public UserProfile create(CreateUserProfileData profileData) {

        //TODO complete the following:
        //Check for duplicate email?  Or make it a unique constraint on the DB?
        //1: call idam to register new user and get an IDAM ID
        //2:Create a User Profile entity
        //3: create db row for new user
        //4: Create a User Profile Resource to return to the client app

        //TODO should call Idam service
        String idamId = "idamId";
        //String idamId = identityManagerService.registerUser(profileData);

        UserProfile userProfile =
            new UserProfile(
                idamId,
                profileData.getEmail(),
                profileData.getFirstName(),
                profileData.getLastName());

        return userProfileRepository.save(userProfile);
    }

}
