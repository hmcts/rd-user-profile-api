package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.ForbiddenException;
import uk.gov.hmcts.reform.userprofileapi.repository.IdamRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

import java.util.Optional;


@Service
public class ValidationServiceImpl implements ValidationService {
    private static final String EMAIL_CLAIM = "email";

    @Autowired
    private AuditService auditService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private ValidationHelperService validationHelperService;

    @Autowired
    private IdamRepository idamRepository;

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

    @Override
    public void validateUserEmailMatchesAuthenticatedPrincipal(String userEmail) {
        if (!StringUtils.hasText(userEmail)) {
            return;
        }

        JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
        String authenticatedEmail = jwtAuthenticationToken.getToken().getClaimAsString(EMAIL_CLAIM);
        if (!StringUtils.hasText(authenticatedEmail)) {
            UserInfo userInfo = idamRepository.getUserInfo(jwtAuthenticationToken.getToken().getTokenValue());
            authenticatedEmail = userInfo.getName();
        }

        if (!userEmail.trim().equalsIgnoreCase(authenticatedEmail)) {
            throw new ForbiddenException("UserEmail header does not match authenticated user");
        }
    }

    @Override
    public void validateUserIdMatchesAuthenticatedPrincipal(String userId) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        JwtAuthenticationToken jwtAuthenticationToken = getJwtAuthenticationToken();
        String authenticatedSubject = jwtAuthenticationToken.getToken().getSubject();
        if (!StringUtils.hasText(authenticatedSubject)) {
            throw new ForbiddenException("Could not determine authenticated user");
        }
        if (!userId.trim().equalsIgnoreCase(authenticatedSubject.trim())) {
            throw new ForbiddenException("userId does not match authenticated user");
        }
    }

    private JwtAuthenticationToken getJwtAuthenticationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof JwtAuthenticationToken jwtAuthenticationToken)) {
            throw new ForbiddenException("Could not determine authenticated user");
        }
        return jwtAuthenticationToken;
    }
}
