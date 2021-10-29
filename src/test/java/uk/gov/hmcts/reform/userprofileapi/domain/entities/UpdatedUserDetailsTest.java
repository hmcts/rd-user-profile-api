package uk.gov.hmcts.reform.userprofileapi.domain.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UpdatedUserDetailsTest {

    @Test
    void test_Update() {
        final String forename = "April";
        final String surname = "O'Neil";
        final boolean active = true;
        UpdatedUserDetails sut = new UpdatedUserDetails(forename, "O'Neil", active);

        assertThat(sut.getForename()).isEqualTo(forename);
        assertThat(sut.getSurname()).isEqualTo(surname);
        assertThat(sut.getActive()).isEqualTo(active);
        assertThat(sut.active).isTrue();
    }

    @Test
    void test_testUpdate_for_lombok_annotations() {
        UpdatedUserDetails sut = new UpdatedUserDetails();
        assertThat(sut).isNotNull();

        sut.setActive(true);
        assertThat(sut.active).isTrue();
        sut.setForename("firstName");
        assertThat(sut.getForename()).isEqualTo("firstName");
        sut.setSurname("lastName");
        assertThat(sut.getSurname()).isEqualTo("lastName");
    }

}
