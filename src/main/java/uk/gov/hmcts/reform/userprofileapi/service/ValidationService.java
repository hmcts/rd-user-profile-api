package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.Optional;

import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ValidationService {

    Optional<UserProfile> validateUpdate(UpdateUserProfileData updateUserProfileData, String userId);
}