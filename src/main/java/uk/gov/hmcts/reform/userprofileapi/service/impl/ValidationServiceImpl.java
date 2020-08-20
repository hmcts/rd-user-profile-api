package uk.gov.hmcts.reform.userprofileapi.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;


@Service
public class ValidationServiceImpl implements ValidationService {

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ValidationHelperService validationHelperService;


    @Override
    public UserProfile validateUpdate(UpdateUserProfileData updateUserProfileData, String userId,
                                      ResponseSource source) {
        // validate input
        validationHelperService.validateUserId(userId);

        // retrieve user by id
        Optional<UserProfile> result = userProfileRepository.findByIdamId(userId);

        // validate with exception that user is well-formed
        validationHelperService.validateUserIsPresent(result);

        validationHelperService.validateUpdateUserProfileRequestValid(updateUserProfileData, userId, source);

        return result.orElse(new UserProfile());
    }

    @Override
    public boolean isValidForUserDetailUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile,
                                              ResponseSource source) {
        return validationHelperService.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, source);
    }

    public boolean isExuiUpdateRequest(String origin) {
        return ResponseSource.EXUI.name().equalsIgnoreCase(origin);

    }

}
