package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import org.junit.Test;

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

    @Test
    public void checkEnums() {
        assertTrue(returnTrueIfValid(UserProfileField.LANGUAGEPREFERENCE));
        assertTrue(returnTrueIfValid(UserProfileField.STATUS));
        assertTrue(returnTrueIfValid(UserProfileField.SYNC));
        assertTrue(returnTrueIfValid(UserProfileField.USERCATEGORY));
        assertTrue(returnTrueIfValid(UserProfileField.USERTYPE));
        assertFalse(returnTrueIfValid(null));
    }

    private boolean returnTrueIfValid(UserProfileField userProfileField) {
        return EnumSet.allOf(UserProfileField.class).contains(userProfileField);
    }
}
