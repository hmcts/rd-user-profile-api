package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import org.junit.Test;

public class LanguagePreferenceTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(LanguagePreference.EN.toString()).isEqualTo("EN");
        assertThat(LanguagePreference.CY.toString()).isEqualTo("CY");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(LanguagePreference.valueOf("EN")).isEqualTo(LanguagePreference.EN);
        assertThat(LanguagePreference.valueOf("CY")).isEqualTo(LanguagePreference.CY);
    }


    @Test
    public void checkEnums() {
        assertTrue(returnTrueIfValid(LanguagePreference.EN));
        assertTrue(returnTrueIfValid(LanguagePreference.CY));
        assertFalse(returnTrueIfValid(null));
    }

    private boolean returnTrueIfValid(LanguagePreference languagePreference) {
        return EnumSet.allOf(LanguagePreference.class).contains(languagePreference);
    }

}
