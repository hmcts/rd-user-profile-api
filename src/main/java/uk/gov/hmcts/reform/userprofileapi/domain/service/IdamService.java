package uk.gov.hmcts.reform.userprofileapi.domain.service;

import feign.FeignException;
import feign.RetryableException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdamUserResponse;

@Slf4j
@Component
public class IdamService implements IdentityManagerService {

    @Autowired
    IdamFeignClient idamClient;

    @Override
    public IdamRegistrationInfo registerUser(CreateUserProfileData requestData) {
        HttpStatus httpStatus;
        ResponseEntity response = null;
        try {
            response = idamClient.createUserProfile(requestData);
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromIdam(ex);
            return new IdamRegistrationInfo(httpStatus);
        }
        return new IdamRegistrationInfo(response.getStatusCode());
    }

    @Override
    public IdamRolesInfo getUserById(UserProfile userProfile) {
        log.info("Getting Idam roles by id for user id:" + userProfile.getIdamId());
        List<String> roles = new ArrayList<String>();
        ResponseEntity<IdamUserResponse> response;
        HttpStatus httpStatus;

        try {
            response = idamClient.getUserById(userProfile.getIdamId().toString());
        } catch (FeignException ex) {
            httpStatus = gethttpStatusFromIdam(ex);
            return new IdamRolesInfo(null, httpStatus);
        }
        return new IdamRolesInfo(response.getBody().getRoles(), response.getStatusCode());
    }

    private HttpStatus gethttpStatusFromIdam(FeignException ex) {
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
