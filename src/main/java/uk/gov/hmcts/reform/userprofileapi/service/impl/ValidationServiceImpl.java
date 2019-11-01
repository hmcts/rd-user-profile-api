package uk.gov.hmcts.reform.userprofileapi.service.impl;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.*;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.CreationChannel;
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
    public UserProfile validateUpdate(UpdateUserProfileData updateUserProfileData, String userId, ResponseSource source) {
        // validate input
        validationHelperService.validateUserIdWithException(userId);

        // retrieve user by id
        Optional<UserProfile> result = userProfileRepository.findByIdamId(userId);

        // validate with exception that user is well-formed
        validationHelperService.validateUserIsPresentWithException(result, userId);

        validationHelperService.validateUpdateUserProfileRequestValid(updateUserProfileData, userId, source);

        return result.orElse(new UserProfile());
    }

    @Override
    public boolean isValidForUserDetailUpdate(UpdateUserProfileData updateUserProfileData, UserProfile userProfile, ResponseSource source) {
        return validationHelperService.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile, source);
    }

    @Override
    public boolean isApiUpdateRequest(String origin) {
        return !CreationChannel.SYNC.name().equalsIgnoreCase(origin);
    }

}
