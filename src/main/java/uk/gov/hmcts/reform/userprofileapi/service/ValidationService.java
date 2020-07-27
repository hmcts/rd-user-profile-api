package uk.gov.hmcts.reform.userprofileapi.service;

import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

public interface ValidationService {

    UserProfile validateUpdate(UpdateUserProfileData updateUserProfileData, String userId, ResponseSource source);

    boolean isValidForUserDetailUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile,
                                       ResponseSource source);

    boolean isExuiUpdateRequest(String origin);
}