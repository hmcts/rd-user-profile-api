package uk.gov.hmcts.reform.userprofileapi.domain.enums;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.EnumSet;
import org.junit.Test;

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

    @Test
    public void checkEnums() {
        assertTrue(returnTrueIfValid(UserCategory.CASEWORKER));
        assertTrue(returnTrueIfValid(UserCategory.PROFESSIONAL));
        assertTrue(returnTrueIfValid(UserCategory.JUDICIAL));
        assertTrue(returnTrueIfValid(UserCategory.CITIZEN));
        assertFalse(returnTrueIfValid(null));
    }

    private boolean returnTrueIfValid(UserCategory userCategory) {
        return EnumSet.allOf(UserCategory.class).contains(userCategory);
    }
}
