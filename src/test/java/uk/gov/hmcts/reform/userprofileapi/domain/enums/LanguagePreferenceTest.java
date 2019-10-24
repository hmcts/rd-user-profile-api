package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;

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

}
