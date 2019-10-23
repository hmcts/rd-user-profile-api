package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface ValidationService {

    UserProfile validateUpdate(UpdateUserProfileData updateUserProfileData, String userId);
}
