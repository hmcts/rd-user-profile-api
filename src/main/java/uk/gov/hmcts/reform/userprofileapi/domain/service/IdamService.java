package uk.gov.hmcts.reform.userprofileapi.domain.service;

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
        ResponseEntity response = idamClient.createUserProfile(requestData);
        return new IdamRegistrationInfo(response.getStatusCode());
    }

    @Override
    public IdamRolesInfo getUserById(UserProfile userProfile) {
        log.info("Getting Idam roles by id for user id:" + userProfile.getIdamId());
        List<String> roles = new ArrayList<String>();

        ResponseEntity<IdamUserResponse> response = idamClient.getUserById(userProfile.getIdamId().toString());
        HttpStatus idamResponseStatus = response.getStatusCode();
        if (idamResponseStatus.is2xxSuccessful()) {
            roles = response.getBody().getRoles();
        }
        return new IdamRolesInfo(roles, idamResponseStatus);
    }
}
