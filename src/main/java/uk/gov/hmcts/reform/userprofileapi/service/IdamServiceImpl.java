package uk.gov.hmcts.reform.userprofileapi.service;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.client.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.client.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;

@Slf4j
@Component
public class IdamServiceImpl implements IdamService {

    @Autowired
    private IdamFeignClient idamClient;

    @Override
    public IdamRegistrationInfo registerUser(IdamRegisterUserRequest requestData) {
        IdamRegistrationInfo result;
        try (Response response = idamClient.createUserProfile(requestData)) {
            ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty());
            result = new IdamRegistrationInfo(entity.getStatusCode(), Optional.ofNullable(entity));
        } catch (FeignException ex) {
            result = new IdamRegistrationInfo(gethttpStatusFromFeignException(ex));
        }
        return result;
    }

    @Override
    public IdamRolesInfo fetchUserById(String id) {
        log.info("Getting Idam roles by id for user id:" + id);
        IdamRolesInfo result;
        try (Response response = idamClient.getUserById(id)) {
            result = buildIdamResponseResult(response);
        } catch (FeignException ex) {
            result = buildIdamResponseFromFeignException(ex);
        }
        return result;
    }

    @Override
    public IdamRolesInfo fetchUserByEmail(String email) {
        log.info("Getting Idam roles by id for user email:" + email);
        IdamRolesInfo result;
        try (Response response = idamClient.getUserByEmail(email)) {
            result = buildIdamResponseResult(response);
        } catch (FeignException ex) {
            result = buildIdamResponseFromFeignException(ex);
        }
        return result;
    }

    @Override
    public IdamRolesInfo updateUserRoles(List roleRequest, String userId) {
        log.info("Update idam roles for userId :" + userId);
        HttpStatus httpStatus = null;
        Response response;
        try {
            response = idamClient.updateUserRoles(roleRequest, userId);
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromFeignException(ex);
        }

        return new IdamRolesInfo(httpStatus);
    }

    @Override
    public IdamRolesInfo addUserRoles(List roleRequest, String userId) {
        log.info("add idam roles for userId :" + userId);
        HttpStatus httpStatus = null;
        Response response;
        try {
            response = idamClient.addUserRoles(roleRequest, userId);
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromFeignException(ex);
        }

        return new IdamRolesInfo(httpStatus);
    }

    public HttpStatus gethttpStatusFromFeignException(FeignException ex) {
        return (ex instanceof RetryableException)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(ex.status());
    }

    private IdamRolesInfo buildIdamResponseResult(Response response) {
        ResponseEntity<IdamUserResponse> entity = JsonFeignResponseHelper.toResponseEntity(response, Optional.of(IdamUserResponse.class));
        return new IdamRolesInfo(entity, entity.getStatusCode());
    }

    private IdamRolesInfo buildIdamResponseFromFeignException(FeignException ex) {
        return new IdamRolesInfo(gethttpStatusFromFeignException(ex));
    }
}
