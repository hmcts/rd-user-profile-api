package uk.gov.hmcts.reform.userprofileapi.service.impl;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
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
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper.getResponseMapperClass;

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
            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setIdamRegistrationResponse(HttpStatus.UNAUTHORIZED);
                result.setStatusMessage(IdamStatusResolver.MISSING_TOKEN);
            }
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
            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setResponseStatusCode(HttpStatus.UNAUTHORIZED);
                result.setStatusMessage(IdamStatusResolver.MISSING_TOKEN);
            }
            log.debug("Inside Fetch User by ID " + result.getResponseStatusCode() + result.getStatusMessage());
        } catch (FeignException ex) {
            result = buildIdamResponseFromFeignException(ex);
        }
        log.debug("At the end of the block fetchUserById repeated log" + result.getResponseStatusCode()
                + result.getId());
        return result;
    }

    @Override
    public IdamRolesInfo updateUserRoles(List roleRequest, String userId) {

        ResponseEntity<Object> responseEntity;
        Response response = null;
        try {
            response = idamClient.updateUserRoles(roleRequest, userId);
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, getResponseMapperClass(response,
                    null));
        } catch (FeignException ex) {
            responseEntity = ResponseEntity.status(gethttpStatusFromFeignException(ex)).build();
        }
        IdamRolesInfo result = getIdamRolesInfo(responseEntity, response);
        return result;
    }

    @Override
    public IdamRolesInfo addUserRoles(Set roleRequest, String userId) {

        ResponseEntity<Object> responseEntity;
        Response response = null;
        try {
            response = idamClient.addUserRoles(roleRequest, userId);
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, getResponseMapperClass(response,
                    null));
        } catch (FeignException ex) {
            responseEntity = ResponseEntity.status(gethttpStatusFromFeignException(ex)).build();
        }
        IdamRolesInfo result = getIdamRolesInfo(responseEntity, response);
        return result;
    }

    IdamRolesInfo getIdamRolesInfo(ResponseEntity<Object> responseEntity, Response response) {
        IdamRolesInfo result = new IdamRolesInfo(responseEntity);

        Optional<Response> respOptional = Optional.ofNullable(response);
        if (respOptional.isPresent()) {
            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setResponseStatusCode(HttpStatus.UNAUTHORIZED);
                result.setStatusMessage(IdamStatusResolver.MISSING_TOKEN);
            }
        }

        return result;
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
            responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return new AttributeResponse(responseEntity);
    }

    public HttpStatus gethttpStatusFromFeignException(FeignException ex) {
        return (ex instanceof RetryableException)
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.valueOf(ex.status());
    }

    @SuppressWarnings("unchecked")
    private IdamRolesInfo buildIdamResponseResult(Response response) {

        ResponseEntity<Object> entity = JsonFeignResponseHelper.toResponseEntity(response,
                getResponseMapperClass(response, IdamUserResponse.class));
        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(entity);

        return idamRolesInfo;
    }

    private IdamRolesInfo buildIdamResponseFromFeignException(FeignException ex) {
        return new IdamRolesInfo(ResponseEntity.status(gethttpStatusFromFeignException(ex)).build());
    }
}
