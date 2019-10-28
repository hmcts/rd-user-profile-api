package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class UpdatedUserDetailsTest {

    @Test
    public void testUpdate() {
        final String forename = "April";
        final String surname = "O'Neil";
        final boolean active = true;
        UpdatedUserDetails sut = new UpdatedUserDetails(forename, "O'Neil", active);

        assertThat(sut.getForename()).isEqualTo(forename);
        assertThat(sut.getSurname()).isEqualTo(surname);
        assertThat(sut.getActive()).isEqualTo(active);
        assertThat(sut.active).isTrue();
    }

}