package uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.idam;

import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.service.IdentityManagerService;
import uk.gov.hmcts.reform.userprofileapi.domain.service.ResourceNotFoundException;
//import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.*;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.feign.IdamFeignClient;

@Slf4j
@Component
public class IdamService implements IdentityManagerService {

    @Autowired
    IdamFeignClient idamClient;
    @Override
    public IdamRegistrationInfo registerUser(CreateUserProfileData requestData) {
        ResponseEntity response = idamClient.createUserProfile(requestData);
        return new IdamRegistrationInfo(response.getStatusCode());
    }

    @Override
    public IdamRolesInfo getUserById(UUID userId) {
        log.info("Getting Idam roles by id for user id:" + userId);
        ResponseEntity<IdamGetUserResponse> response = idamClient.getUserById(userId.toString());
        if (HttpStatus.OK == response.getStatusCode()) {
            return new IdamRolesInfo(response.getBody().getRoles());
        }
        else{
            throw new ResourceNotFoundException("Get Idam user info failed");
        }
    }

 /*   @Override
    public IdamRolesInfo getIdamUserDetailsByEmail(String email) {
    log.info("Getting idam roles by email for emailId:" + email);
        ResponseEntity<IdamGetUserResponse> response = idamClient.getUserByEmail(email);
        if (HttpStatus.OK == response.getStatusCode()) {
            return new IdamRolesInfo(response.getBody().getRoles());
        }
        else{
            throw new ResourceNotFoundException("Get Idam user info failed");
        }
    }*/
}
