package uk.gov.hmcts.reform.userprofileapi.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;

public class UserProfileIdentifierTest {

    @Test
    public void test_hold_values_after_creation() {
        UserProfileIdentifier userProfileIdentifierWithOneValue = new UserProfileIdentifier(IdentifierName.EMAIL,
                "test_email@hmcts.net");
        UserProfileIdentifier userProfileIdentifierWithMultipleValue
                = new UserProfileIdentifier(IdentifierName.UUID_LIST, new ArrayList<>(Arrays.asList("UUID1", "UUID2",
                "UUID3")));

        assertThat(userProfileIdentifierWithOneValue.getName()).isEqualTo(IdentifierName.EMAIL);
        assertThat(userProfileIdentifierWithOneValue.getValue()).isEqualTo("test_email@hmcts.net");

        assertThat(userProfileIdentifierWithMultipleValue.getName()).isEqualTo(IdentifierName.UUID_LIST);
        assertThat(userProfileIdentifierWithMultipleValue.getValues()).contains("UUID1");
        assertThat(userProfileIdentifierWithMultipleValue.getValues()).contains("UUID2");
        assertThat(userProfileIdentifierWithMultipleValue.getValues()).contains("UUID3");
    }

}
