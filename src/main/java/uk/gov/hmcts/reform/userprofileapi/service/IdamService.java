package uk.gov.hmcts.reform.userprofileapi.service;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public interface IdamService {

    IdamRegistrationInfo registerUser(IdamRegisterUserRequest requestData);

    IdamRolesInfo fetchUserById(String id);

    IdamRolesInfo updateUserRoles(List<String> roleRequest, String userId);

    IdamRolesInfo addUserRoles(Set<Map<String, String>>  roleRequest, String userId);

    AttributeResponse updateUserDetails(UpdateUserDetails updateUserDetails, String userId);

}