package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class IdamStatusTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(IdamStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(IdamStatus.DELETED.toString()).isEqualTo("DELETED");
        assertThat(IdamStatus.PENDING.toString()).isEqualTo("PENDING");
        assertThat(IdamStatus.SUSPENDED.toString()).isEqualTo("SUSPENDED");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(IdamStatus.valueOf("ACTIVE")).isEqualTo(IdamStatus.ACTIVE);
        assertThat(IdamStatus.valueOf("DELETED")).isEqualTo(IdamStatus.DELETED);
        assertThat(IdamStatus.valueOf("PENDING")).isEqualTo(IdamStatus.PENDING);
        assertThat(IdamStatus.valueOf("SUSPENDED")).isEqualTo(IdamStatus.SUSPENDED);
    }

}
