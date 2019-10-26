package uk.gov.hmcts.reform.userprofileapi.service.impl;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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

        //Determine if status needs to be updated
        if (userProfile.getStatus().equals(IdamStatus.SUSPENDED)
                && !userProfile.getStatus().name().equalsIgnoreCase(updateUserProfileData.getIdamStatus())) {
            idamClient.updateUserDetails(updateUserProfileData, userId);
        }

        //set valid fields
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

        UserProfile userProfile = validateUserStatusWithException(userId);

        Optional<RoleAdditionResponse> roleAdditionResponseOpt = assignRolesAndPersistAudit(profileData, userId, userProfile);

        roleAdditionResponseOpt.ifPresent(userProfileResponse::setAddRolesResponse);

        log.info("Delete idam roles for userId :" + userId);//TODO remove unnecessary logging
        List<RoleDeletionResponse> roleDeletionResponse = profileData.getRolesDelete().stream()
                .map(role -> deleteRolesInIdam(userId, role.getName(), userProfile))
                .collect(Collectors.toList());

        userProfileResponse.setDeleteRolesResponse(roleDeletionResponse);

        return userProfileResponse;
    }

    private Optional<RoleAdditionResponse> assignRolesAndPersistAudit(UpdateUserProfileData profileData, String userId, UserProfile userProfile) {
        if (!CollectionUtils.isEmpty(profileData.getRolesAdd())) {
            log.info("Add idam roles for userId :" + userId);//TODO remove unnecessary logging

            Optional<HttpStatus> httpStatusOpt;
            try (Response response = idamClient.addUserRoles(profileData.getRolesAdd(), userId)) {
                httpStatusOpt = Optional.of(JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode());
            } catch (FeignException ex) {
                httpStatusOpt = Optional.of(getHttpStatusFromFeignException(ex));
                auditService.persistAudit(httpStatusOpt.get(), userProfile, ResponseSource.API);
            }
            return Optional.of(new RoleAdditionResponse(httpStatusOpt.get()));
        }
        return Optional.empty();
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

    private UserProfile validateUserStatusWithException(String userId) {
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId);
        if (userProfileOptional.isPresent() && null != userProfileOptional.get().getStatus()) {
            return userProfileOptional.get();
        }

        throw new ResourceNotFoundException("could not find user profile for userId: or status is not active " + userId);
    }

}

