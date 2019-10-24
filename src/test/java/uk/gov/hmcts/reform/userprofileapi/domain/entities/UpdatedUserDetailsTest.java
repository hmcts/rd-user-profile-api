package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdatedUserDetailsTest {

    @Test
    public void testUpdate() {
        final String forename = "April";
        final String surname = "O'Neil";
        UpdatedUserDetails sut = new UpdatedUserDetails(forename, "O'Neil", true);

        assertThat(sut.getForename()).isEqualTo(forename);
        assertThat(sut.getSurname()).isEqualTo(surname);
        assertThat(sut.active).isTrue();

    }

}