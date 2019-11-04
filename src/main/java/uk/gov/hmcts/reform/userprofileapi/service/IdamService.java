package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;

@Component
public interface IdamService {

    IdamRegistrationInfo registerUser(IdamRegisterUserRequest requestData);

    IdamRolesInfo fetchUserById(String id);

    IdamRolesInfo fetchUserByEmail(String email);

    IdamRolesInfo updateUserRoles(List roleRequest, String userId);

    IdamRolesInfo addUserRoles(Set roleRequest, String userId);

    AttributeResponse updateUserDetails(UpdateUserDetails updateUserDetails, String userId);

}