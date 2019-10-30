package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public interface ValidationHelperService {

    boolean validateUserIdWithException(String userId);

    boolean validateUserIsPresentWithException(Optional<UserProfile> userProfile, String userId);

    boolean validateUpdateUserProfileRequestValid(UpdateUserProfileData updateUserProfileData, String userId);
}
