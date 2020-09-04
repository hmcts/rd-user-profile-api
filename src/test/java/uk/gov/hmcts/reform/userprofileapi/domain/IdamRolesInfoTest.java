package uk.gov.hmcts.reform.userprofileapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;

public class IdamRolesInfoTest {

    @Test
    public void test_populate_all_fields() {
        Boolean active = true;
        String email = "some@hmcts.net";
        String foreName = "firstName";
        String userId = UUID.randomUUID().toString();
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manger");
        String surName = "lastName";
        Boolean pending = false;

        IdamUserResponse idamUserResponse = new IdamUserResponse(active, email, foreName, userId, pending, roles,
                surName);
        ResponseEntity<Object> entity = new ResponseEntity<>(idamUserResponse, HttpStatus.CREATED);

        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(entity);

        assertThat(idamRolesInfo.getEmail()).isEqualTo(email);
        assertThat(idamRolesInfo.getForename()).isEqualTo(foreName);
        assertThat(idamRolesInfo.getId()).isEqualTo(userId);
        assertThat(idamRolesInfo.getSurname()).isEqualTo(surName);
        assertThat(idamRolesInfo.getRoles()).isNotEmpty();
        assertThat(idamRolesInfo.getResponseStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(idamRolesInfo.getStatusMessage()).isNotEmpty();
        assertThat(idamRolesInfo.isSuccessFromIdam()).isTrue();
    }

    @Test
    public void test_isSuccessFromIdam_ReturnsFalseWhenStatusIs400() {
        IdamUserResponse idamUserResponse = mock(IdamUserResponse.class);
        ResponseEntity<Object> entity = new ResponseEntity<>(idamUserResponse, HttpStatus.BAD_REQUEST);

        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(entity);

        assertThat(idamRolesInfo.getResponseStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(idamRolesInfo.getStatusMessage()).isNotEmpty();

        assertThat(idamRolesInfo.isSuccessFromIdam()).isFalse();
    }

    @Test
    public void idamResponseNullWhenStatusIs400() {
        IdamUserResponse idamUserResponse = null;
        ResponseEntity<Object> entity = new ResponseEntity<>(idamUserResponse, HttpStatus.BAD_REQUEST);

        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(entity);
        assertThat(idamUserResponse).isNull();
        assertThat(idamRolesInfo.getStatusMessage())
                .isEqualTo("13 Required parameters or one of request field is missing or invalid");
        assertThat(idamRolesInfo.isSuccessFromIdam()).isFalse();
    }
}
