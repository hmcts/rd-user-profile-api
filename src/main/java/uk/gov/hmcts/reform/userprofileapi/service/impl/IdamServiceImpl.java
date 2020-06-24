package uk.gov.hmcts.reform.userprofileapi.service.impl;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;

@Slf4j
@Component
public class IdamServiceImpl implements IdamService {

    @Autowired
    private IdamFeignClient idamClient;

    @Value("${logging-component-name}")
    protected String loggingComponentName;

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
        //Getting Idam roles by id
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
        //Getting Idam roles by user email
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
        //Update idam roles
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
    public IdamRolesInfo addUserRoles(Set roleRequest, String userId) {
        //add idam roles
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

    @Override
    public AttributeResponse updateUserDetails(UpdateUserDetails updateUserDetails, String userId) {
        //Update user details
        HttpStatus httpStatus = null;
        Response response;
        try {
            response = idamClient.updateUserDetails(updateUserDetails, userId);
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Optional.empty()).getStatusCode();
        } catch (FeignException ex) {
            log.error(loggingComponentName,"SIDAM call failed:", ex);
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new AttributeResponse(httpStatus);
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
