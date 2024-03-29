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
            log.debug("in Idam to create user profile");
            Response response = idamClient.createUserProfile(requestData);
            ResponseEntity<Object> entity = JsonFeignResponseHelper.toResponseEntity(response,
                    getResponseMapperClass(response, null));
            result = new IdamRegistrationInfo(entity);

            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setIdamRegistrationResponse(HttpStatus.UNAUTHORIZED);
                result.setStatusMessage(IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE);
            }

            log.debug("after idam response" + result.getStatusMessage() + result.getIdamRegistrationResponse());

        } catch (FeignException ex) {
            result = new IdamRegistrationInfo(ResponseEntity.status(gethttpStatusFromFeignException(ex)).build());
        }
        return result;
    }

    @Override
    public IdamRolesInfo fetchUserById(String id) {

        IdamRolesInfo result;
        try {
            log.debug("In Before calling IdamFeignClient" + "," + id);
            Response response = idamClient.getUserById(id);
            log.debug("After calling IdamFeignClient");
            result = buildIdamResponseResult(response);
            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setResponseStatusCode(HttpStatus.UNAUTHORIZED);
                result.setStatusMessage(IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE);
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
        return getIdamRolesInfo(responseEntity, response);
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
        return getIdamRolesInfo(responseEntity, response);
    }

    IdamRolesInfo getIdamRolesInfo(ResponseEntity<Object> responseEntity, Response response) {
        IdamRolesInfo result = new IdamRolesInfo(responseEntity);

        Optional<Response> respOptional = Optional.ofNullable(response);
        if (respOptional.isPresent()) {
            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setResponseStatusCode(HttpStatus.UNAUTHORIZED);
                result.setStatusMessage(IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE);
            }
        }

        return result;
    }

    @Override
    public AttributeResponse updateUserDetails(UpdateUserDetails updateUserDetails, String userId) {
        //Update user details
        ResponseEntity<Object> responseEntity = null;
        Response response = null;
        try {
            response = idamClient.updateUserDetails(updateUserDetails, userId);
            responseEntity = JsonFeignResponseHelper.toResponseEntity(response, getResponseMapperClass(response,
                    null));
        } catch (FeignException ex) {
            log.error("{}:: {} {}", loggingComponentName, "SIDAM call failed:", ex);
            responseEntity = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        AttributeResponse result = new AttributeResponse(responseEntity);
        Optional<Response> respOptional = Optional.ofNullable(response);
        if (respOptional.isPresent()) {
            HttpStatus httpStatus = HttpStatus.valueOf(response.status());
            if (httpStatus.is5xxServerError()) {
                result.setIdamStatusCode(HttpStatus.UNAUTHORIZED.value());
                result.setIdamMessage(IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE);
            }
        }
        return result;
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
        return new IdamRolesInfo(entity);
    }

    public IdamRolesInfo buildIdamResponseFromFeignException(FeignException ex) {
        return new IdamRolesInfo(ResponseEntity.status(gethttpStatusFromFeignException(ex)).build());
    }
}
