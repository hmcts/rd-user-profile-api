package uk.gov.hmcts.reform.userprofileapi.service;

import feign.FeignException;
import feign.Response;
import feign.RetryableException;
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

import java.util.List;
import java.util.Optional;


@Slf4j
@Component
public class IdamServiceImpl implements IdamService {

    @Autowired
    private IdamFeignClient idamClient;

    @Override
    public IdamRegistrationInfo registerUser(CreateUserProfileData requestData) {
        IdamRegistrationInfo result = null;
        try {
            Response response = idamClient.createUserProfile(requestData);
            ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, null);
            result = new IdamRegistrationInfo(entity.getStatusCode(), entity);
        } catch (FeignException ex) {
            result = new IdamRegistrationInfo(gethttpStatusFromFeignException(ex), null);
        }
        return result;
    }

    @Override
    public IdamRolesInfo fetchUserById(String id) {
        log.info("Getting Idam roles by id for user id:" + id);
        return handleIdamClientException(new IdCommand<ResponseEntity<IdamUserResponse>>(), id);
    }

    @Override
    public IdamRolesInfo fetchUserByEmail(String email) {
        log.info("Getting Idam roles by id for user email:" + email);
        return handleIdamClientException(new EmailCommand<ResponseEntity<IdamUserResponse>>(), email);
    }

    @Override
    public IdamRolesInfo updateUserRoles(List roleRequest, String userId) {
        log.info("Update idam roles for userId :" + userId);
        HttpStatus httpStatus = null;
        Response response = null;
        try {
            response = idamClient.updateUserRoles(roleRequest, userId);
            httpStatus = JsonFeignResponseHelper.toResponseEntity(response, Response.class).getStatusCode();
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromFeignException(ex);
        }
        Optional<ResponseEntity<IdamUserResponse>> entity = Optional.empty();
        return new IdamRolesInfo(entity, httpStatus);
    }

    public HttpStatus gethttpStatusFromFeignException(FeignException ex) {
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

    @SuppressWarnings("unchecked")
    private IdamRolesInfo handleIdamClientException(Command command, String queryParam){
        IdamRolesInfo result;
        Optional<ResponseEntity<IdamUserResponse>> entity;
        try {
            entity = ((Optional<ResponseEntity<IdamUserResponse>> ) command.execute(queryParam));
            result = new IdamRolesInfo(entity, entity.get().getStatusCode());
        } catch(FeignException ex) {
            HttpStatus httpStatus = gethttpStatusFromFeignException(ex);
            result = new IdamRolesInfo(Optional.empty(), httpStatus);
        }
        return result;
    }

    interface Command<T> {
        T execute(String queryArg);
    }

    class EmailCommand<T> implements Command {

        @Override
        @SuppressWarnings("unchecked")
        public T execute(String queryArg) {
            return (T) Optional.of(JsonFeignResponseHelper.toResponseEntity(idamClient.getUserByEmail(queryArg), IdamUserResponse.class));
        }
    }

    class IdCommand<T> implements Command {

        @Override
        @SuppressWarnings("unchecked")
        public T execute(String queryArg) {
            return (T) Optional.of(JsonFeignResponseHelper.toResponseEntity(idamClient.getUserById(queryArg), IdamUserResponse.class));
        }
    }



}