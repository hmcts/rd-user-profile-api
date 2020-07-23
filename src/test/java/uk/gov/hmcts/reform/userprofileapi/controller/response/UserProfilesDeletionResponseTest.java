package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class UserProfilesDeletionResponseTest {

    @Test
    public void test_DeleteOrganisationResponseTest() {

        final int statusCode = 204;
        final String message = "successfully deleted";

        final UserProfilesDeletionResponse userProfilesDeletionResponse = new UserProfilesDeletionResponse();
        userProfilesDeletionResponse.setStatusCode(statusCode);
        userProfilesDeletionResponse.setMessage(message);
        assertThat(userProfilesDeletionResponse.getStatusCode()).isEqualTo(statusCode);
        assertThat(userProfilesDeletionResponse.getMessage()).isEqualTo(message);
    }

}