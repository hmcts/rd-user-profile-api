package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ResponseSourceTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(ResponseSource.API.toString()).isEqualTo("API");
        assertThat(ResponseSource.EXUI.toString()).isEqualTo("EXUI");
        assertThat(ResponseSource.SYNC.toString()).isEqualTo("SYNC");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(ResponseSource.valueOf("API")).isEqualTo(ResponseSource.API);
        assertThat(ResponseSource.valueOf("EXUI")).isEqualTo(ResponseSource.EXUI);
        assertThat(ResponseSource.valueOf("SYNC")).isEqualTo(ResponseSource.SYNC);
    }
}
