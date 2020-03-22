package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public interface ValidationHelperService {

    boolean validateUserIdWithException(String userId);

    boolean validateUserIsPresentWithException(Optional<UserProfile> userProfile);

    boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId, ResponseSource source);

    boolean validateUserStatusBeforeUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile, ResponseSource source);

    boolean validateUserPersistedWithException(HttpStatus status);

    UserProfile validateReInvitedUser(Optional<UserProfile> userProfileOpt);

    boolean validateUserLastUpdatedWithinSpecifiedTimeWithException(UserProfile userProfile, long expectedHours);

    boolean validateUserStatusWithException(UserProfile userProfile, IdamStatus expectedStatus);
}
