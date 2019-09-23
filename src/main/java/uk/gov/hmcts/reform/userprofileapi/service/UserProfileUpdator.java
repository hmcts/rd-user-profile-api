package uk.gov.hmcts.reform.userprofileapi.service;

import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isSameAsExistingUserProfile;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUpdateUserProfileRequestValid;
import static uk.gov.hmcts.reform.userprofileapi.util.UserProfileValidator.isUserIdValid;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;

import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.InvalidRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.RequiredFieldMissingException;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.AuditRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;


@Service
@Slf4j
public class UserProfileUpdator implements ResourceUpdator<UpdateUserProfileData> {

    @Autowired
    private IdamService idamService;
    @Autowired
    private UserProfileRepository userProfileRepository;
    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private IdamFeignClient idamClient;

    @Override
    public UserProfile update(UpdateUserProfileData updateUserProfileData, String userId) {

        HttpStatus status = HttpStatus.OK;
        UserProfile userProfile = null;
        if (!isUserIdValid(userId, false)) {
            persistAudit(HttpStatus.NOT_FOUND, null, ResponseSource.SYNC);
            throw new ResourceNotFoundException("userId provided is malformed");
        }

        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId);
        userProfile =  userProfileOptional.orElse(null);

        if (userProfile == null) {
            persistAudit(HttpStatus.NOT_FOUND, null, ResponseSource.SYNC);
            throw new ResourceNotFoundException("could not find user profile for userId: " + userId);
        } else if (!isUpdateUserProfileRequestValid(updateUserProfileData)) {
            persistAudit(HttpStatus.BAD_REQUEST, null,ResponseSource.SYNC);
            throw new RequiredFieldMissingException("Update user profile request is not valid for userId: " + userId);
        } else if (!isSameAsExistingUserProfile(updateUserProfileData, userProfile)) {

            userProfile.setEmail(updateUserProfileData.getEmail().trim());
            userProfile.setFirstName(updateUserProfileData.getFirstName().trim());
            userProfile.setLastName(updateUserProfileData.getLastName().trim());
            userProfile.setStatus(IdamStatus.valueOf(updateUserProfileData.getIdamStatus().toUpperCase()));
        }

        try {
            userProfile = userProfileRepository.save(userProfile);
        } catch (Exception ex) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        persistAudit(status, userProfile, ResponseSource.SYNC);
        return userProfile;
    }

    public UserProfileRolesResponse updateRoles(UpdateUserProfileData profileData, String userId) {
        log.info("Inside updateRoles method");
        UserProfile userProfile;
        HttpStatus httpStatus = null;
        userProfile = validateUserStatus(userId);
        if (!CollectionUtils.isEmpty(profileData.getRolesAdd())) {
            try (Response response = idamClient.addUserRoles(profileData.getRolesAdd(), userId)) {
                httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();

            } catch (FeignException ex) {
                httpStatus = getHttpStatusFromFeignException(ex);
                persistAudit(httpStatus, userProfile,ResponseSource.API);
            }
        }
        log.info("Exiting updateRoles method");
        return new UserProfileRolesResponse(httpStatus);
    }

    public HttpStatus getHttpStatusFromFeignException(FeignException ex) {
        return (ex instanceof RetryableException)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(ex.status());
    }

    public void persistAudit(HttpStatus idamStatus, UserProfile userProfile, ResponseSource responseSource) {
        Audit audit = new Audit(idamStatus.value(), resolveStatusAndReturnMessage(idamStatus), responseSource, userProfile);
        auditRepository.save(audit);
    }


    private UserProfile validateUserStatus(String userId) {
        UserProfile userProfile = null;
        Optional<UserProfile> userProfileOptional = userProfileRepository.findByIdamId(userId);
        userProfile = userProfileOptional.orElse(null);
        if (userProfile == null) {
            throw new ResourceNotFoundException("could not find user profile for userId: or status is not active " + userId);
        } else if (!IdamStatus.ACTIVE.equals(userProfileOptional.get().getStatus())) {
            throw new InvalidRequest("UserId status is not active");
        }
        return userProfileOptional.get();
    }


}
