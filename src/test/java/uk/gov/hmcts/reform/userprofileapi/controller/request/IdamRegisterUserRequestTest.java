package uk.gov.hmcts.reform.userprofileapi.controller.request;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class IdamRegisterUserRequestTest {

    @Test
    void test_populate_fields() {
        String email = "test@test.com";
        String firstName = "fname";
        String lastName = "lname";
        String id = UUID.randomUUID().toString();
        List<String> roles = new ArrayList<>();
        roles.add("pui_case_manager");

        IdamRegisterUserRequest idamRegisterUserRequest = new IdamRegisterUserRequest(email, firstName, lastName, id,
                roles);

        assertThat(idamRegisterUserRequest.getEmail()).isEqualTo(email);
        assertThat(idamRegisterUserRequest.getFirstName()).isEqualTo(firstName);
        assertThat(idamRegisterUserRequest.getLastName()).isEqualTo(lastName);
        assertThat(idamRegisterUserRequest.getId()).isEqualTo(id);
        assertThat(idamRegisterUserRequest.getRoles()).isNotEmpty();
        assertThat(idamRegisterUserRequest.getRoles().get(0)).isEqualTo("pui_case_manager");
    }
}
