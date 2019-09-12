package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;
import org.springframework.http.HttpStatus;

public class UserProfileRolesResponseTest {


    @Test
    public void should_Return_User_profile_Resposne() {

        UserProfileRolesResponse userProfileRolesResponse = new UserProfileRolesResponse(HttpStatus.OK);
        assertThat(userProfileRolesResponse.getResponseStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(userProfileRolesResponse.getStatusMessage()).isEqualTo("11 OK");
    }
}