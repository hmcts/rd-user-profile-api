package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import java.util.Optional;

public interface ValidationService {

    Optional<UserProfile> validateUpdate(UpdateUserProfileData updateUserProfileData, String userId);
}
