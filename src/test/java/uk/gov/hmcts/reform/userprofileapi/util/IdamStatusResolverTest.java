package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.ACCEPTED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.MISSING_TOKEN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.NO_CONTENT;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.OK;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.TOKEN_EXPIRED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.UNKNOWN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.USER_EXISTS;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

@RunWith(MockitoJUnitRunner.class)
public class IdamStatusResolverTest {

    @Test
    public void should_return_error_message_by_HttpStatus_provided() {
        String httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.OK);
        assertThat(httpStatusString).isEqualTo(OK);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CREATED);
        assertThat(httpStatusString).isEqualTo(ACCEPTED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.BAD_REQUEST);
        assertThat(httpStatusString).isEqualTo(INVALID_REQUEST);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.UNAUTHORIZED);
        assertThat(httpStatusString).isEqualTo(MISSING_TOKEN);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(TOKEN_EXPIRED);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.NOT_FOUND);
        assertThat(httpStatusString).isEqualTo(NOT_FOUND);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.CONFLICT);
        assertThat(httpStatusString).isEqualTo(USER_EXISTS);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.NO_CONTENT);
        assertThat(httpStatusString).isEqualTo(NO_CONTENT);

        httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.MULTI_STATUS);
        assertThat(httpStatusString).isEqualTo(UNKNOWN);
    }

    @Test
    public void should_resolve_and_return_idam_status_by_idam_flags() {
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(false,true)))
                .isEqualTo(IdamStatus.PENDING);
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(true,false)))
                .isEqualTo(IdamStatus.ACTIVE);
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(false,false)))
                .isEqualTo(IdamStatus.SUSPENDED);
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
        List<String> roles = Collections.singletonList("pui-case-manger");
        String surName = "lastName";

        IdamUserResponse idamUserResponse = new IdamUserResponse(active, email, foreName, userId,pending,
                roles, surName);
        ResponseEntity<IdamUserResponse> entity = new ResponseEntity<IdamUserResponse>(idamUserResponse,
                HttpStatus.CREATED);
        return new IdamRolesInfo(entity, HttpStatus.CREATED);
    }

    @Test
    public void privateConstructorTest_for_IdamStatusResolver() throws Exception {
        Constructor<IdamStatusResolver> constructor = IdamStatusResolver.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }
}
