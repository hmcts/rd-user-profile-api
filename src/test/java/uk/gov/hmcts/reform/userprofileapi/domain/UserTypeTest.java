package uk.gov.hmcts.reform.userprofileapi.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UserTypeTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(UserType.EXTERNAL.toString()).isEqualTo("EXTERNAL");
        assertThat(UserType.INTERNAL.toString()).isEqualTo("INTERNAL");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(UserType.valueOf("EXTERNAL")).isEqualTo(UserType.EXTERNAL);
        assertThat(UserType.valueOf("INTERNAL")).isEqualTo(UserType.INTERNAL);
    }

}
