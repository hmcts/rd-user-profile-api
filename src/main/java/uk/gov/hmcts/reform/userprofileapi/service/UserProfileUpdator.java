package uk.gov.hmcts.reform.userprofileapi.service;

import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUpdateUserProfileRequestValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;

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
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileMapper;


@Service
@Slf4j
public class UserProfileUpdator implements ResourceUpdator<UpdateUserProfileData> {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private IdamFeignClient idamClient;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private AuditService auditService;

    @Override
    public AttributeResponse update(UpdateUserProfileData profileData, String userId, String origin) {
        return null;
    }

    @Override
    public Optional<UserProfile> update(UpdateUserProfileData updateUserProfileData, String userId) {

        if (!isUserIdValid(userId, false)) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            throw new ResourceNotFoundException("userId provided is malformed");
        }

        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId);

        if (!userProfileOptional.isPresent()) {
            auditService.persistAudit(HttpStatus.NOT_FOUND, ResponseSource.SYNC);
            throw new ResourceNotFoundException("could not find user profile for userId: " + userId);
        }

        if (!isUpdateUserProfileRequestValid(updateUserProfileData)) {
            auditService.persistAudit(HttpStatus.BAD_REQUEST, ResponseSource.SYNC);
            throw new RequiredFieldMissingException("Update user profile request is not valid for userId: " + userId);
        }

        UserProfileMapper.mapUpdatableFields(updateUserProfileData, userProfileOptional.orElse(null));

        return doPersistUserProfile(userProfileOptional.get());
    }

    private Optional<UserProfile> doPersistUserProfile(UserProfile userProfile) {
        UserProfile result = null;
        HttpStatus status = HttpStatus.OK;
        try {
            result = userProfileRepository.save(userProfile);
        } catch (Exception ex) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        auditService.persistAudit(status, result, ResponseSource.SYNC);
        return Optional.ofNullable(result);
    }

    @Override
    public UserProfileRolesResponse updateRoles(UpdateUserProfileData profileData, String userId) {
        UserProfile userProfile;
        HttpStatus httpStatus;
        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse();
        userProfile = validateUserStatus(userId);
        if (!CollectionUtils.isEmpty(profileData.getRolesAdd())) {
            log.info("Add idam roles for userId :" + userId);
            AddRoleResponse addRolesResponse = new AddRoleResponse();
            try (Response response = idamClient.addUserRoles(profileData.getRolesAdd(), userId)) {
                httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
                addRolesResponse.loadStatusCodes(httpStatus);
            } catch (FeignException ex) {
                httpStatus = getHttpStatusFromFeignException(ex);
                auditService.persistAudit(httpStatus, userProfile, ResponseSource.API);
                addRolesResponse.loadStatusCodes(httpStatus);
            }
            userProfileRolesResponse.setAddRolesResponse(addRolesResponse);
        }

        if (!CollectionUtils.isEmpty(profileData.getRolesDelete())) {

            log.info("Delete idam roles for userId :" + userId);
            List<DeleteRoleResponse> deleteRoleResponses = new ArrayList<>();
            UserProfile finalUserProfile = userProfile;
            profileData.getRolesDelete().forEach(role -> deleteRoleResponses.add(deleteRolesInIdam(userId, role.getName(), finalUserProfile)));
            userProfileRolesResponse.setDeleteRolesResponse(deleteRoleResponses);
        }
        return userProfileRolesResponse;
    }

    private DeleteRoleResponse deleteRolesInIdam(String userId, String roleName, UserProfile userProfile) {
        HttpStatus httpStatus;
        try (Response response = idamClient.deleteUserRole(userId, roleName)) {
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
        } catch (FeignException ex) {
            httpStatus = getHttpStatusFromFeignException(ex);
            auditService.persistAudit(httpStatus, userProfile, ResponseSource.API);
        }
        return new DeleteRoleResponse(roleName, httpStatus);
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

