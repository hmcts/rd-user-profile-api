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

import uk.gov.hmcts.reform.userprofileapi.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleAdditionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.service.AuditService;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.service.ResourceUpdator;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileMapper;


@Service
@Slf4j
public class UserProfileResourceUpdator implements ResourceUpdator<UpdateUserProfileData> {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private IdamFeignClient idamClient;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuditService auditService;

    @Override
    public Optional<UserProfile> update(UpdateUserProfileData updateUserProfileData, String userId) {
        Optional<UserProfile> userProfileOptional = validationService.validateUpdate(updateUserProfileData, userId);

        if(userProfileOptional.isPresent()) {
            UserProfileMapper.mapUpdatableFields(updateUserProfileData, userProfileOptional.get());
            return Optional.ofNullable(doPersistUserProfile(userProfileOptional.get()));
        }

        auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
        throw new ResourceNotFoundException("Could not find user profile for userId:" + userId);
    }

    @Override
    public AttributeResponse update(UpdateUserProfileData profileData, String userId, String origin) {
        //TODO Impl
        return null;
    }

    @Override
    public UserProfileRolesResponse updateRoles(UpdateUserProfileData profileData, String userId) {
        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        UserProfile userProfile = validateUserStatus(userId);

        if (!CollectionUtils.isEmpty(profileData.getRolesAdd())) {
            userProfileRolesResponse = addIdamRolesByUserId(profileData, userId, new UserProfileRolesResponse());
        }

        if (!CollectionUtils.isEmpty(profileData.getRolesDelete())) {
            deleteIdamRolesByUserId(profileData, userId, userProfile, userProfileRolesResponse);
        }

        return userProfileRolesResponse;
    }

    private UserProfile doPersistUserProfile(UserProfile userProfile) {
        UserProfile result = null;
        HttpStatus status = HttpStatus.OK;
        try {
            result = userProfileRepository.save(userProfile);
        } catch (Exception ex) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        } finally {
            auditService.persistAudit(status, (null != result) ? result : userProfile, ResponseSource.SYNC);
        }
        return result;
    }

    private UserProfileRolesResponse addIdamRolesByUserId(UpdateUserProfileData profileData, String userId, UserProfileRolesResponse userProfileRolesResponse) {
        HttpStatus httpStatus;
        UserProfile userProfile = validateUserStatus(userId);

        RoleAdditionResponse addRolesResponse;
        try (Response response = idamClient.addUserRoles(profileData.getRolesAdd(), userId)) {
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
        } catch (FeignException ex) {
            httpStatus = getHttpStatusFromFeignException(ex);
            auditService.persistAudit(httpStatus, userProfile, ResponseSource.API);
        }

        addRolesResponse = new RoleAdditionResponse(httpStatus);
        userProfileRolesResponse.setAddRolesResponse(addRolesResponse);
        return userProfileRolesResponse;
    }

    private void deleteIdamRolesByUserId(UpdateUserProfileData profileData, String userId, UserProfile userProfile, UserProfileRolesResponse userProfileRolesResponse) {
        List<RoleDeletionResponse> roleDeletionRespons = new ArrayList<>();
        profileData.getRolesDelete().forEach(role -> roleDeletionRespons.add(deleteRolesInIdam(userId, role.getName(), userProfile)));
        userProfileRolesResponse.setDeleteRolesResponse(roleDeletionRespons);
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

