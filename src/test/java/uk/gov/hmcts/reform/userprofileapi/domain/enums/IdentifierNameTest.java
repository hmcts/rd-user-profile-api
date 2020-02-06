package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IdentifierNameTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(IdentifierName.EMAIL.toString()).isEqualTo("EMAIL");
        assertThat(IdentifierName.UUID.toString()).isEqualTo("UUID");
        assertThat(IdentifierName.UUID_LIST.toString()).isEqualTo("UUID_LIST");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(IdentifierName.valueOf("EMAIL")).isEqualTo(IdentifierName.EMAIL);
        assertThat(IdentifierName.valueOf("UUID")).isEqualTo(IdentifierName.UUID);
        assertThat(IdentifierName.valueOf("UUID_LIST")).isEqualTo(IdentifierName.UUID_LIST);
    }
}
