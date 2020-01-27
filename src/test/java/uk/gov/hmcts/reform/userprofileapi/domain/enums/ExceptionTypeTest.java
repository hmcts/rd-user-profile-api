package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class ExceptionTypeTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(ExceptionType.ERRORPERSISTINGEXCEPTION.toString()).isEqualTo("ERRORPERSISTINGEXCEPTION");
        assertThat(ExceptionType.IDAMSERVICEEXCEPTION.toString()).isEqualTo("IDAMSERVICEEXCEPTION");
        assertThat(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION.toString()).isEqualTo("REQUIREDFIELDMISSINGEXCEPTION");
        assertThat(ExceptionType.RESOURCENOTFOUNDEXCEPTION.toString()).isEqualTo("RESOURCENOTFOUNDEXCEPTION");
        assertThat(ExceptionType.UNDEFINDEDEXCEPTION.toString()).isEqualTo("UNDEFINDEDEXCEPTION");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(ExceptionType.valueOf("ERRORPERSISTINGEXCEPTION")).isEqualTo(ExceptionType.ERRORPERSISTINGEXCEPTION);
        assertThat(ExceptionType.valueOf("IDAMSERVICEEXCEPTION")).isEqualTo(ExceptionType.IDAMSERVICEEXCEPTION);
        assertThat(ExceptionType.valueOf("REQUIREDFIELDMISSINGEXCEPTION")).isEqualTo(ExceptionType.REQUIREDFIELDMISSINGEXCEPTION);
        assertThat(ExceptionType.valueOf("RESOURCENOTFOUNDEXCEPTION")).isEqualTo(ExceptionType.RESOURCENOTFOUNDEXCEPTION);
        assertThat(ExceptionType.valueOf("UNDEFINDEDEXCEPTION")).isEqualTo(ExceptionType.UNDEFINDEDEXCEPTION);
    }
}
