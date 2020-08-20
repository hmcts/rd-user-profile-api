package uk.gov.hmcts.reform.userprofileapi.controller.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UpdateUserDetailsTest {

    @Test
    public void test_hold_values_after_creation() {
        String firstName = "fname";
        String lastName = "lname";
        Boolean statusTrue = Boolean.TRUE;

        UpdateUserDetails updateUserDetails = new UpdateUserDetails(firstName, lastName, Boolean.TRUE);

        assertThat(updateUserDetails.getForename()).isEqualTo(firstName);
        assertThat(updateUserDetails.getSurname()).isEqualTo(lastName);
        assertThat(updateUserDetails.getActive()).isEqualTo(statusTrue);
    }
}