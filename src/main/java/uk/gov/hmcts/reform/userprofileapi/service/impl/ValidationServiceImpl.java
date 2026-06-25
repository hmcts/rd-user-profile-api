package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

import java.util.Optional;

import static org.apache.commons.lang.StringUtils.isNotBlank;

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

        if (isNotBlank(updateUserProfileData.getIdamId())) {
            // validate that the userid that you are trying to update does not already exist
            Optional<UserProfile> userResult = userProfileRepository.findByIdamId(updateUserProfileData.getIdamId());

            validationHelperService.validateUserAlreadyExists(userResult);

        }

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
