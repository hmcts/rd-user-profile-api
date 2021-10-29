package uk.gov.hmcts.reform.userprofileapi.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

class IdamRegistrationInfoTest {

    private IdamRegistrationInfo sut;

    private ResponseEntity<Object> responseEntityMock;


    @BeforeEach
    public void setUp() throws Exception {
        responseEntityMock = status(ACCEPTED).build();

        sut = new IdamRegistrationInfo(responseEntityMock);
    }

    @Test
    void test_OneArgConstructor() {
        final String expectMessage = resolveStatusAndReturnMessage(UNAUTHORIZED);

        IdamRegistrationInfo sut = new IdamRegistrationInfo(status(UNAUTHORIZED).build());
        String actualMessage = sut.getStatusMessage();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    void test_isSuccessFromIdam() {
        IdamRegistrationInfo sut = new IdamRegistrationInfo(responseEntityMock);
        Boolean actual = sut.isSuccessFromIdam();

        assertThat(actual).isTrue();
    }

    @Test
    void test_isDuplicateUser() {
        IdamRegistrationInfo sut = new IdamRegistrationInfo(status(CONFLICT).build());
        assertThat(sut.isDuplicateUser()).isTrue();
    }

    @Test
    void test_getIdamRegistrationResponse() {
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(ACCEPTED);
    }

}
