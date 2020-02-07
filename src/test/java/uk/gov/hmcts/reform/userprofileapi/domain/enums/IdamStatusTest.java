package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
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

    @Test
    public void checkEnums() {
        assertTrue(returnTrueIfValidIdamStatus(IdamStatus.ACTIVE));
        assertTrue(returnTrueIfValidIdamStatus(IdamStatus.DELETED));
        assertTrue(returnTrueIfValidIdamStatus(IdamStatus.PENDING));
        assertTrue(returnTrueIfValidIdamStatus(IdamStatus.SUSPENDED));
        assertFalse(returnTrueIfValidIdamStatus(null));
    }

    private boolean returnTrueIfValidIdamStatus(IdamStatus idamStatus) {
        return EnumSet.allOf(IdamStatus.class).contains(idamStatus);
    }
}


