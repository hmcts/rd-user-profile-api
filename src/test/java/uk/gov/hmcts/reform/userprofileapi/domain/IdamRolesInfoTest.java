package uk.gov.hmcts.reform.userprofileapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;

public class IdamRolesInfoTest {

    @Test
    public void should_populate_all_fields() {
        Boolean active = true;
        String email = "some@hmcts.net";
        String foreName = "firstName";
        String userId = UUID.randomUUID().toString();
        List<String> roles = new ArrayList<>();
        roles.add("pui-case-manger");
        String surName = "lastName";
        Boolean pending = false;

        IdamUserResponse idamUserResponse = new IdamUserResponse(active, email, foreName, userId, pending, roles, surName);
        ResponseEntity<Object> entity = new ResponseEntity<>(idamUserResponse, HttpStatus.CREATED);

        IdamRolesInfo idamRolesInfo = new IdamRolesInfo(entity);

        assertThat(idamRolesInfo.getEmail()).isEqualTo(email);
        assertThat(idamRolesInfo.getForename()).isEqualTo(foreName);
        assertThat(idamRolesInfo.getId()).isEqualTo(userId);
        assertThat(idamRolesInfo.getSurname()).isEqualTo(surName);
        assertThat(idamRolesInfo.getRoles()).isNotEmpty();
        assertThat(idamRolesInfo.getResponseStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(idamRolesInfo.getStatusMessage()).isNotEmpty();
        assertThat(idamRolesInfo.isSuccessFromIdam()).isEqualTo(true);
    }
}
