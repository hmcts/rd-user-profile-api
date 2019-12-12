package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;

public class UserCategoryTest {

    @Test
    public void should_return_correct_string_values() {
        assertThat(UserCategory.CASEWORKER.toString()).isEqualTo("CASEWORKER");
        assertThat(UserCategory.PROFESSIONAL.toString()).isEqualTo("PROFESSIONAL");
        assertThat(UserCategory.JUDICIAL.toString()).isEqualTo("JUDICIAL");
        assertThat(UserCategory.CITIZEN.toString()).isEqualTo("CITIZEN");
    }

    @Test
    public void should_return_correct_enum_from_string() {
        assertThat(UserCategory.valueOf("CASEWORKER")).isEqualTo(UserCategory.CASEWORKER);
        assertThat(UserCategory.valueOf("PROFESSIONAL")).isEqualTo(UserCategory.PROFESSIONAL);
        assertThat(UserCategory.valueOf("JUDICIAL")).isEqualTo(UserCategory.JUDICIAL);
        assertThat(UserCategory.valueOf("CITIZEN")).isEqualTo(UserCategory.CITIZEN);
    }

}
