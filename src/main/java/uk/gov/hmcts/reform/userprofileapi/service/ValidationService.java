package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public interface ValidationService {

    UserProfile validateUpdate(UpdateUserProfileData updateUserProfileData, String userId);
}
