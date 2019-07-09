package uk.gov.hmcts.reform.userprofileapi.service;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;

@Slf4j
@Component
public class IdamService implements IdentityManagerService {

    @Autowired
    IdamFeignClient idamClient;

    @Override
    public IdamRegistrationInfo registerUser(CreateUserProfileData requestData) {
        HttpStatus httpStatus;
        Response response = null;
        try {
            response = idamClient.createUserProfile(requestData);
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromIdam(ex);
            return new IdamRegistrationInfo(httpStatus, null);
        }
        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, null);
        return new IdamRegistrationInfo(entity.getStatusCode(), entity);
    }

    @Override
    public IdamRolesInfo getUserById(String id) {
        log.info("Getting Idam roles by id for user id:" + id);
        return getUserFromIdam(null, id);
    }

    @Override
    public IdamRolesInfo searchUserByEmail(String email) {

        log.info("Getting Idam roles by id for user email:" + email);
        return getUserFromIdam(email, null);
    }

    @Override
    public IdamRolesInfo updateUserRoles(List roleRequest, String userId) {
        log.info("Update idam roles for userId :" + userId);
        HttpStatus httpStatus;
        Response response = null;
        try {
            response = idamClient.updateUserRoles(roleRequest, userId);
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromIdam(ex);
            return new IdamRolesInfo(null, httpStatus);
        }
        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, null);
        return new IdamRolesInfo(null, entity.getStatusCode());
    }

    private IdamRolesInfo getUserFromIdam(String email, String idamId) {
        Response response = null;
        HttpStatus httpStatus;

        try {
            if (email != null) {
                response = idamClient.getUserByEmail(email);
            } else {
                response = idamClient.getUserById(idamId);
            }

        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromIdam(ex);
            return new IdamRolesInfo(null, httpStatus);
        }
        ResponseEntity<IdamUserResponse> entity = JsonFeignResponseHelper.toResponseEntity(response, IdamUserResponse.class);
        return new IdamRolesInfo(entity, entity.getStatusCode());
    }

    public HttpStatus gethttpStatusFromIdam(FeignException ex) {
        HttpStatus httpStatus;
        log.error("Idam returned status : " + ex.status());
        if (ex instanceof RetryableException) {
            log.error("Converted Feign exception to 500:UNKNOWN because connection timed out");
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        } else {
            httpStatus = HttpStatus.valueOf(ex.status());
        }
        return httpStatus;
    }
}
