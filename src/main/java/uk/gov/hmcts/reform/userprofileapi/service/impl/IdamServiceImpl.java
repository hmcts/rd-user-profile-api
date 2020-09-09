package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper.getResponseMapperClass;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
import java.util.List;
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
@SuppressWarnings("unchecked")
public class IdamServiceImpl implements IdamService {

    @Autowired
    private IdamFeignClient idamClient;

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Override
    public IdamRegistrationInfo registerUser(IdamRegisterUserRequest requestData) {
        IdamRegistrationInfo result;
        try  {
            Response response = idamClient.createUserProfile(requestData);
            ResponseEntity<Object> entity = JsonFeignResponseHelper.toResponseEntity(response,
                    getResponseMapperClass(response, null));
            result = new IdamRegistrationInfo(entity);
        } catch (FeignException ex) {
            result = new IdamRegistrationInfo(ResponseEntity.status(gethttpStatusFromFeignException(ex)).build());
        }
        return result;
    }

    @Override
    public IdamRolesInfo fetchUserById(String id) {

        IdamRolesInfo result;
        try {
            Response response = idamClient.getUserById(id);
            result = buildIdamResponseResult(response);
        } catch (FeignException ex) {
            result = buildIdamResponseFromFeignException(ex);
        }
        return result;
    }

    @Override
    public IdamRolesInfo updateUserRoles(List roleRequest, String userId) {

        ResponseEntity<Object> responseEntity;
        Response response;
        try {
            response = idamClient.updateUserRoles(roleRequest, userId);
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, getResponseMapperClass(response,
                    null));
        } catch (FeignException ex) {
            responseEntity = ResponseEntity.status(gethttpStatusFromFeignException(ex)).build();
        }

        return new IdamRolesInfo(responseEntity);
    }

    @Override
    public IdamRolesInfo addUserRoles(Set roleRequest, String userId) {

        ResponseEntity<Object> responseEntity;
        Response response;
        try {
            response = idamClient.addUserRoles(roleRequest, userId);
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, getResponseMapperClass(response,
                    null));
        } catch (FeignException ex) {
            responseEntity = ResponseEntity.status(gethttpStatusFromFeignException(ex)).build();
        }

        return new IdamRolesInfo(responseEntity);
    }

    @Override
    public AttributeResponse updateUserDetails(UpdateUserDetails updateUserDetails, String userId) {
        //Update user details
        ResponseEntity<Object> responseEntity = null;
        try {
            Response response = idamClient.updateUserDetails(updateUserDetails, userId);
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, getResponseMapperClass(response,
                    null));
        } catch (FeignException ex) {
            log.error("{}:: {} {}", loggingComponentName, "SIDAM call failed:", ex);
            responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return new AttributeResponse(responseEntity);
    }

    public HttpStatus gethttpStatusFromFeignException(FeignException ex) {
        return (ex instanceof RetryableException)
                ? HttpStatus.INTERNAL_SERVER_ERROR
                : HttpStatus.valueOf(ex.status());
    }

    @SuppressWarnings("unchecked")
    private IdamRolesInfo buildIdamResponseResult(Response response) {
        ResponseEntity<Object> entity = JsonFeignResponseHelper.toResponseEntity(response,
                getResponseMapperClass(response, IdamUserResponse.class));
        return new IdamRolesInfo(entity);
    }

    private IdamRolesInfo buildIdamResponseFromFeignException(FeignException ex) {
        return new IdamRolesInfo(ResponseEntity.status(gethttpStatusFromFeignException(ex)).build());
    }
}
