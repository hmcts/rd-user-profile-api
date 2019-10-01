package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.ACCEPTED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.MISSING_TOKEN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.OK;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.TOKEN_EXPIRED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.UNKNOWN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.USER_EXISTS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.client.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@RunWith(MockitoJUnitRunner.class)
public class IdamStatusResolverTest {

    @Test
    public void should_return_error_message_by_HttpStatus_provided() {
        String httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CREATED);
        assertThat(httpStatusString).isEqualTo(ACCEPTED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.OK);
        assertThat(httpStatusString).isEqualTo(OK);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.BAD_REQUEST);
        assertThat(httpStatusString).isEqualTo(INVALID_REQUEST);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.UNAUTHORIZED);
        assertThat(httpStatusString).isEqualTo(MISSING_TOKEN);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(TOKEN_EXPIRED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(TOKEN_EXPIRED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.NOT_FOUND);
        assertThat(httpStatusString).isEqualTo(NOT_FOUND);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CONFLICT);
        assertThat(httpStatusString).isEqualTo(USER_EXISTS);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.ALREADY_REPORTED);
        assertThat(httpStatusString).isEqualTo(UNKNOWN);
    }

    @Test
    public void should_resolve_and_return_idam_status_by_idam_flags() {

        Map<Map<String, Boolean>, IdamStatus> idamStatusMap = new HashMap<Map<String, Boolean>, IdamStatus>();
        idamStatusMap.put(addRule(false,true), IdamStatus.PENDING);
        idamStatusMap.put(addRule(true, false), IdamStatus.ACTIVE);
        idamStatusMap.put(addRule(false,false), IdamStatus.SUSPENDED);



        assertThat(IdamStatusResolver.resolveIdamStatus(idamStatusMap, createIdamRoleInfo(false,true))).isEqualTo(IdamStatus.PENDING);
        assertThat(IdamStatusResolver.resolveIdamStatus(idamStatusMap, createIdamRoleInfo(true,false))).isEqualTo(IdamStatus.ACTIVE);
        assertThat(IdamStatusResolver.resolveIdamStatus(idamStatusMap, createIdamRoleInfo(false,false))).isEqualTo(IdamStatus.SUSPENDED);
    }


    public Map<String, Boolean> addRule(boolean activeFlag, boolean pendingFlag) {
        Map<String, Boolean> pendingMapWithRules = new HashMap<>();
        pendingMapWithRules.put("ACTIVE", activeFlag);
        pendingMapWithRules.put("PENDING", pendingFlag);
        return pendingMapWithRules;
    }

    public IdamRolesInfo createIdamRoleInfo(Boolean active, Boolean pending) {
        String email = "some@hmcts.net";
        String foreName = "firstName";
        String userId = UUID.randomUUID().toString();
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manger");
        String surName = "lastName";

        IdamUserResponse idamUserResponse = new IdamUserResponse(active, email, foreName, userId,pending, roles, surName);
        ResponseEntity<IdamUserResponse> entity = new ResponseEntity<IdamUserResponse>(idamUserResponse, HttpStatus.CREATED);
        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(entity, HttpStatus.CREATED);
        return idamRolesInfo;
    }
}
