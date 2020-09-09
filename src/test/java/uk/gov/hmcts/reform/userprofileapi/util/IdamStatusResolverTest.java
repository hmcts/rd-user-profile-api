package uk.gov.hmcts.reform.userprofileapi.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.ACCEPTED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.INVALID_REQUEST;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.MISSING_TOKEN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.NO_CONTENT;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.OK;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.TOKEN_EXPIRED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.UNKNOWN;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.USER_EXISTS;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.getErrorMessageFromSidamResponse;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
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
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

@RunWith(MockitoJUnitRunner.class)
public class IdamStatusResolverTest {

    @Test
    public void test_return_error_message_by_HttpStatus_provided() {
        String httpStatusString = IdamStatusResolver.resolveStatusAndReturnMessage(HttpStatus.OK);
        assertThat(httpStatusString).isEqualTo(OK);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.CREATED);
        assertThat(httpStatusString).isEqualTo(ACCEPTED);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.BAD_REQUEST);
        assertThat(httpStatusString).isEqualTo(INVALID_REQUEST);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.UNAUTHORIZED);
        assertThat(httpStatusString).isEqualTo(MISSING_TOKEN);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.FORBIDDEN);
        assertThat(httpStatusString).isEqualTo(TOKEN_EXPIRED);

        httpStatusString = resolveStatusAndReturnMessage(NOT_FOUND);
        assertThat(httpStatusString).isEqualTo(IdamStatusResolver.NOT_FOUND);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.CONFLICT);
        assertThat(httpStatusString).isEqualTo(USER_EXISTS);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.NO_CONTENT);
        assertThat(httpStatusString).isEqualTo(NO_CONTENT);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.MULTI_STATUS);
        assertThat(httpStatusString).isEqualTo(UNKNOWN);

        httpStatusString = resolveStatusAndReturnMessage(HttpStatus.PRECONDITION_FAILED);
        assertThat(httpStatusString).isEqualTo(IdamStatusResolver.PRECONDITION_FAILED);
    }

    @Test
    public void test_resolve_and_return_idam_status_by_idam_flags() {
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(false,true)))
                .isEqualTo(IdamStatus.PENDING);
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(true,false)))
                .isEqualTo(IdamStatus.ACTIVE);
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(false,false)))
                .isEqualTo(IdamStatus.SUSPENDED);

        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(null,null)))
                .isEqualTo(IdamStatus.SUSPENDED);
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(null,false)))
                .isEqualTo(IdamStatus.SUSPENDED);

        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(null,false)))
                .isEqualTo(IdamStatus.SUSPENDED);
        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(null,true)))
                .isEqualTo(IdamStatus.PENDING);

        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(true,null)))
                .isEqualTo(IdamStatus.ACTIVE);

        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(Boolean.TRUE,null)))
                .isEqualTo(IdamStatus.ACTIVE);

        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(null,Boolean.FALSE)))
                .isEqualTo(IdamStatus.SUSPENDED);

        assertThat(IdamStatusResolver.resolveIdamStatus(createIdamRoleInfo(null,Boolean.TRUE)))
                .isEqualTo(IdamStatus.PENDING);

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

        IdamUserResponse idamUserResponse = new IdamUserResponse(active, email, foreName, userId,pending, roles,
                surName);
        ResponseEntity<Object> entity = new ResponseEntity<Object>(idamUserResponse, HttpStatus.CREATED);
        return new IdamRolesInfo(entity);
    }

    @Test
    public void test_IdamStatusResolver_private_constructor() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        Constructor<IdamStatusResolver> constructor = IdamStatusResolver.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    public void test_resolveStatusAndReturnMessage_when_responseEntity_is_null() {
        ResponseEntity<Object> responseEntity = null;
        String errorMessage = resolveStatusAndReturnMessage(responseEntity);
        assertThat(errorMessage).isEqualTo(resolveStatusAndReturnMessage(INTERNAL_SERVER_ERROR));
    }

    @Test
    public void test_resolveStatusAndReturnMessage_when_responseEntity_body_is_null() {
        ResponseEntity<Object> responseEntity = ResponseEntity.status(NOT_FOUND).build();
        String errorMessage = resolveStatusAndReturnMessage(responseEntity);
        assertThat(errorMessage).isEqualTo(resolveStatusAndReturnMessage(NOT_FOUND));
    }

    @Test
    public void test_resolveStatusAndReturnMessage_when_responseEntity_body_has_not_instance_of_IdamErrorResponse() {
        ResponseEntity<Object> responseEntity = ResponseEntity.status(CREATED).body(new ErrorResponse());
        String errorMessage = resolveStatusAndReturnMessage(responseEntity);
        assertThat(errorMessage).isEqualTo(resolveStatusAndReturnMessage(CREATED));
    }

    @Test
    public void test_resolveStatusAndReturnMessage_when_responseEntity_body_has_IdamErrorResponse() {
        IdamErrorResponse idamErrorResponse = getIdamErrorResponse(null,
                "some test error message");
        ResponseEntity<Object> responseEntity = ResponseEntity.status(CREATED).body(idamErrorResponse);
        String errorMessage = resolveStatusAndReturnMessage(responseEntity);
        assertThat(errorMessage).isEqualTo("some test error message");
    }

    @Test
    public void test_getErrorMessageFromSidamResponse_when_responseEntity_body_has_IdamErrorResponse_with_all_fields() {
        List<String> errorMessages = new ArrayList<>();
        errorMessages.add("errorMessage1");
        errorMessages.add("errorMessage2");
        IdamErrorResponse idamErrorResponse = getIdamErrorResponse(errorMessages,
                "some test error message");
        String errorMessage = getErrorMessageFromSidamResponse(idamErrorResponse);
        assertThat(errorMessage).isEqualTo("errorMessage1");
    }

    @Test
    public void test_getErrMsgFromSidamResponse_when_responseEntity_body_has_IdamErrorResponse_with_ErrMsg_field() {
        List<String> errorMessages = new ArrayList<>();
        errorMessages.add("errorMessage1");
        errorMessages.add("errorMessage2");
        IdamErrorResponse idamErrorResponse = getIdamErrorResponse(errorMessages, null);
        String errorMessage = getErrorMessageFromSidamResponse(idamErrorResponse);
        assertThat(errorMessage).isEqualTo("errorMessage1");
    }

    @Test
    public void test_getErrMsgFromSidamResponse_when_responseEntity_body_has_IdamErrResponse_with_ErrMsg_field_empty() {
        IdamErrorResponse idamErrorResponse = getIdamErrorResponse(new ArrayList<String>(), null);
        String errorMessage = getErrorMessageFromSidamResponse(idamErrorResponse);
        assertThat(errorMessage).isNull();
    }

    @Test
    public void test_getErrMsgFromSidamResponse_when_responseEntity_body_has_IdamErrorResponse_with_ErrMsg_fields() {
        IdamErrorResponse idamErrorResponse = getIdamErrorResponse(null,
                "some test error message");
        String errorMessage = getErrorMessageFromSidamResponse(idamErrorResponse);
        assertThat(errorMessage).isEqualTo("some test error message");
    }

    public IdamErrorResponse getIdamErrorResponse(List<String> errorMessages, String errorMessage) {
        IdamErrorResponse idamErrorResponse = new IdamErrorResponse();
        idamErrorResponse.setStatus(400);
        idamErrorResponse.setErrorMessages(errorMessages);
        idamErrorResponse.setErrorMessage(errorMessage);
        return idamErrorResponse;
    }

}
