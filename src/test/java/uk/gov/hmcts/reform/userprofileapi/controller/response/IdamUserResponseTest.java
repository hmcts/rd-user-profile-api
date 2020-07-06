package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;

import org.junit.Test;

public class IdamUserResponseTest {

    @Test
    public void test_hold_values_after_creation() {
        final boolean expectedActive = true;
        final String expectedEmail = "someemail@abc.com";
        final String expectedForename = "forename";
        final String expectedId = "id";
        final boolean expectedPending = false;
        final String expectedLastname = "lastname";

        IdamUserResponse idamUserResponse = new IdamUserResponse(expectedActive, expectedEmail, expectedForename,
                expectedId, expectedPending, new ArrayList<>(), expectedLastname);

        assertThat(idamUserResponse.getActive()).isEqualTo(expectedActive);
        assertThat(idamUserResponse.getEmail()).isEqualTo(expectedEmail);
        assertThat(idamUserResponse.getForename()).isEqualTo(expectedForename);
        assertThat(idamUserResponse.getId()).isEqualTo(expectedId);
        assertThat(idamUserResponse.getPending()).isFalse();
        assertThat(idamUserResponse.getSurname()).isEqualTo(expectedLastname);
        assertThat(idamUserResponse.getRoles()).isNotNull();
    }
}