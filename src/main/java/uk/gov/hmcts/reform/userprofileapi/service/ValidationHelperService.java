package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public interface ValidationHelperService {

    boolean validateUserId(String userId);

    void validateUserIsPresent(Optional<UserProfile> userProfile);

    boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId,
                                                  ResponseSource source);

    boolean validateUserStatusBeforeUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile,
                                           ResponseSource source);

    boolean validateUserPersisted(HttpStatus status);

    UserProfile validateReInvitedUser(Optional<UserProfile> userProfileOpt);

    void validateUserLastUpdatedWithinSpecifiedTime(UserProfile userProfile, long expectedHours);

    void validateUserStatus(UserProfile userProfile, IdamStatus expectedStatus);
}
