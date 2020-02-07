package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import org.junit.Test;

public class UserProfileStatusTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(UserProfileStatus.ACTIVE.toString()).isEqualTo("ACTIVE");
        assertThat(UserProfileStatus.INACTIVE.toString()).isEqualTo("INACTIVE");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(UserProfileStatus.valueOf("ACTIVE")).isEqualTo(UserProfileStatus.ACTIVE);
        assertThat(UserProfileStatus.valueOf("INACTIVE")).isEqualTo(UserProfileStatus.INACTIVE);
    }

    @Test
    public void checkEnums() {
        assertTrue(returnTrueIfValid(UserProfileStatus.ACTIVE));
        assertTrue(returnTrueIfValid(UserProfileStatus.INACTIVE));
        assertFalse(returnTrueIfValid(null));
    }

    private boolean returnTrueIfValid(UserProfileStatus userProfileStatus) {
        return EnumSet.allOf(UserProfileStatus.class).contains(userProfileStatus);
    }
}
