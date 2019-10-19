package uk.gov.hmcts.reform.userprofileapi.service;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.deriveStatusFlag;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isSameAsExistingUserProfile;
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
import org.springframework.util.StringUtils;
import uk.gov.hmcts.reform.userprofileapi.client.AddRoleResponse;
import uk.gov.hmcts.reform.userprofileapi.client.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.client.DeleteRoleResponse;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator;

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
    private IdamService idamService;

    @Override
    public AttributeResponse update(UpdateUserProfileData updateUserProfileData, String userId, String origin) {

        HttpStatus status = HttpStatus.OK;
        UserProfile userProfile = null;
        AttributeResponse response = null;
        boolean isExuiUpdate = UserProfileValidator.isUpdateFromExui(origin);
        ResponseSource source = isExuiUpdate ? ResponseSource.API : ResponseSource.SYNC;
        if (!isUserIdValid(userId, false)) {
            persistAudit(HttpStatus.NOT_FOUND, null, source);
            throw new ResourceNotFoundException("userId provided is malformed");
        }
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId);
        userProfile = userProfileOptional.orElse(null);

        if (userProfile == null) {
            persistAudit(HttpStatus.NOT_FOUND, null, source);
            throw new ResourceNotFoundException("could not find user profile for userId: " + userId);
        } else if (!isUpdateUserProfileRequestValid(updateUserProfileData)) {
            persistAudit(HttpStatus.BAD_REQUEST, userProfile,source);
            throw new RequiredFieldMissingException("Update user profile request is not valid for userId: " + userId);
        } else if (!isSameAsExistingUserProfile(updateUserProfileData, userProfile)) {
            if (isExuiUpdate) {
                response = updateUserDetailsInSidam(userProfile, source, updateUserProfileData, userId);
                status = HttpStatus.valueOf(Integer.valueOf(response.getIdamStatusCode()));
            }
            updateExistingUserProfile(updateUserProfileData, userProfile);
            if (!isExuiUpdate || (status.is2xxSuccessful())) {
                try {
                    userProfile = userProfileRepository.save(userProfile);
                } catch (Exception ex) {
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                }
            }
        }
        persistAudit(status, userProfile, source);
        return response;
    }

    public void updateExistingUserProfile(UpdateUserProfileData updateUserProfileData, UserProfile userProfile) {

        if (!StringUtils.isEmpty(updateUserProfileData.getFirstName())) {
            userProfile.setFirstName(updateUserProfileData.getFirstName().trim());
        }
        if (!StringUtils.isEmpty(updateUserProfileData.getLastName())) {
            userProfile.setLastName(updateUserProfileData.getLastName().trim());
        }
        if (!StringUtils.isEmpty(updateUserProfileData.getIdamStatus().isEmpty())) {
            userProfile.setStatus(IdamStatus.valueOf(updateUserProfileData.getIdamStatus().toUpperCase()));
        }
    }

    public AttributeResponse updateUserDetailsInSidam(UserProfile userProfile, ResponseSource source, UpdateUserProfileData updateUserProfileData, String userId) {
        if (IdamStatus.PENDING == userProfile.getStatus() || IdamStatus.PENDING.toString().equalsIgnoreCase(updateUserProfileData.getIdamStatus())) {
            persistAudit(HttpStatus.BAD_REQUEST, userProfile, source);
            throw new RequiredFieldMissingException("Cannot change status to PENDING or user has already PENDING status");
        }
        UpdateUserDetails data = new UpdateUserDetails(updateUserProfileData.getFirstName(), updateUserProfileData.getLastName(), deriveStatusFlag(updateUserProfileData));
        return idamService.updateUserDetails(data, userId);
    }

    @Override
    public UserProfileRolesResponse updateRoles(UpdateUserProfileData profileData, String userId) {
        UserProfile userProfile = null;
        HttpStatus httpStatus = null;
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
                persistAudit(httpStatus, userProfile, ResponseSource.API);
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
            persistAudit(httpStatus, userProfile, ResponseSource.API);
        }
        return new DeleteRoleResponse(roleName, httpStatus);
    }

    private HttpStatus getHttpStatusFromFeignException(FeignException ex) {
        return (ex instanceof RetryableException)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(ex.status());
    }

    private void persistAudit(HttpStatus idamStatus, UserProfile userProfile, ResponseSource responseSource) {
        Audit audit = new Audit(idamStatus.value(), resolveStatusAndReturnMessage(idamStatus), responseSource, userProfile);
        auditRepository.save(audit);
    }

    private void persistAudit(HttpStatus idamStatus, ResponseSource responseSource) {
        Audit audit = new Audit(idamStatus.value(), resolveStatusAndReturnMessage(idamStatus), responseSource);
        auditRepository.save(audit);
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

