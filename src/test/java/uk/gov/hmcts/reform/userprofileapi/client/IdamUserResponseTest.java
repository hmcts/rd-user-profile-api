package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;


public class IdamUserResponseTest {

    @Test
    public void should_hold_values_after_creation() {

        IdamUserResponse idamUserResponse =
                new IdamUserResponse(
                        true,
                        "someemail@abc.com",
                        "forename",
                        "id",
                        false,
                        new ArrayList<String>(),
                        "lastname");


        assertThat(idamUserResponse.getActive()).isEqualTo(true);
        assertThat(idamUserResponse.getEmail()).isEqualTo("someemail@abc.com");
        assertThat(idamUserResponse.getForename()).isEqualTo("forename");
        assertThat(idamUserResponse.getId()).isEqualTo("id");
        assertThat(idamUserResponse.getRoles()).isNotNull();

    }
}
