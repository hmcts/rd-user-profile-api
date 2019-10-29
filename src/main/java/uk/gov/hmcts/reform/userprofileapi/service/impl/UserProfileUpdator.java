package uk.gov.hmcts.reform.userprofileapi.service.impl;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleAdditionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.exception.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileMapper;

@Service
@Slf4j
public class UserProfileUpdator implements ResourceUpdator<UpdateUserProfileData> {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private IdamFeignClient idamClient;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuditService auditService;

    @Override
    public Optional<UserProfileResponse> update(UpdateUserProfileData updateUserProfileData, String userId, ResponseSource origin) {

        UserProfile userProfile = validationService.validateUpdate(updateUserProfileData, userId);

        if (validationService.isValidForUserDetailUpdate(updateUserProfileData, userProfile)) {
            idamClient.updateUserDetails(updateUserProfileData, userId);
        }

        UserProfileMapper.mapUpdatableFields(updateUserProfileData, userProfile);

        Optional<UserProfile> userProfileOpt = doPersistUserProfile(userProfile, origin);

        return userProfileOpt.map(opt -> new UserProfileResponse(opt, false));
    }


    private Optional<UserProfile> doPersistUserProfile(UserProfile userProfile, ResponseSource responseSource) {
        UserProfile result = null;
        HttpStatus status = HttpStatus.OK;
        try {
            result = userProfileRepository.save(userProfile);
        } catch (Exception ex) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        auditService.persistAudit(status, result, responseSource);
        return Optional.ofNullable(result);
    }


    @Override
    public UserProfileResponse updateRoles(UpdateUserProfileData profileData, String userId) {
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        UserProfile userProfile = validateUserStatus(userId);
        if (!CollectionUtils.isEmpty(profileData.getRolesAdd())) {
            log.info("Add idam roles for userId :" + userId);
            RoleAdditionResponse addRolesResponse;
            HttpStatus httpStatus;
            try (Response response = idamClient.addUserRoles(profileData.getRolesAdd(), userId)) {
                httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
                addRolesResponse = new RoleAdditionResponse(httpStatus);
            } catch (FeignException ex) {
                httpStatus = getHttpStatusFromFeignException(ex);
                auditService.persistAudit(httpStatus, userProfile, ResponseSource.API);
                addRolesResponse = new RoleAdditionResponse(httpStatus);
            }
            userProfileResponse.setAddRolesResponse(addRolesResponse);
        }

        if (!CollectionUtils.isEmpty(profileData.getRolesDelete())) {
            log.info("Delete idam roles for userId :" + userId);
            List<RoleDeletionResponse> roleDeletionResponse = new ArrayList<>();
            profileData.getRolesDelete().forEach(role -> roleDeletionResponse.add(deleteRolesInIdam(userId, role.getName(), userProfile)));
            userProfileResponse.setDeleteRolesResponse(roleDeletionResponse);
        }
        return userProfileResponse;
    }

    private RoleDeletionResponse deleteRolesInIdam(String userId, String roleName, UserProfile userProfile) {
        HttpStatus httpStatus;
        try (Response response = idamClient.deleteUserRole(userId, roleName)) {
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
        } catch (FeignException ex) {
            httpStatus = getHttpStatusFromFeignException(ex);
            auditService.persistAudit(httpStatus, userProfile, ResponseSource.API);
        }
        return new RoleDeletionResponse(roleName, httpStatus);
    }

    private HttpStatus getHttpStatusFromFeignException(FeignException ex) {
        return (ex instanceof RetryableException)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(ex.status());
    }

    private UserProfile validateUserStatus(String userId) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId);
        if (!userProfileOptional.isPresent()) {
            throw new ResourceNotFoundException("could not find user profile for userId: or status is not active " + userId);
        } else if (!IdamStatus.ACTIVE.equals(userProfileOptional.get().getStatus())) {
            throw new InvalidRequest("UserId status is not active");
        }
        return userProfileOptional.get();
    }

}

