package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UserProfileFieldTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(UserProfileField.LANGUAGEPREFERENCE.toString()).isEqualTo("LANGUAGEPREFERENCE");
        assertThat(UserProfileField.STATUS.toString()).isEqualTo("STATUS");
        assertThat(UserProfileField.SYNC.toString()).isEqualTo("SYNC");
        assertThat(UserProfileField.USERCATEGORY.toString()).isEqualTo("USERCATEGORY");
        assertThat(UserProfileField.USERTYPE.toString()).isEqualTo("USERTYPE");

    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(UserProfileField.valueOf("LANGUAGEPREFERENCE")).isEqualTo(UserProfileField.LANGUAGEPREFERENCE);
        assertThat(UserProfileField.valueOf("STATUS")).isEqualTo(UserProfileField.STATUS);
        assertThat(UserProfileField.valueOf("SYNC")).isEqualTo(UserProfileField.SYNC);
        assertThat(UserProfileField.valueOf("USERCATEGORY")).isEqualTo(UserProfileField.USERCATEGORY);
        assertThat(UserProfileField.valueOf("USERTYPE")).isEqualTo(UserProfileField.USERTYPE);

    }
}
