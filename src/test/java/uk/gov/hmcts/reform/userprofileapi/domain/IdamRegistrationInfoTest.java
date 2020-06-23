package uk.gov.hmcts.reform.userprofileapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.ResponseEntity.status;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@SuppressWarnings("unchecked")
public class IdamRegistrationInfoTest {

    private IdamRegistrationInfo sut;

    private ResponseEntity responseEntityMock;


    @Before
    public void setUp() throws Exception {
        responseEntityMock = ResponseEntity.status(ACCEPTED).build();

        sut = new IdamRegistrationInfo(responseEntityMock);
    }

    @Test
    public void testOneArgConstructor() {
        final HttpStatus inputMessage = UNAUTHORIZED;
        final String expectMessage = resolveStatusAndReturnMessage(inputMessage);

        IdamRegistrationInfo sut = new IdamRegistrationInfo(status(UNAUTHORIZED).build());
        String actualMessage = sut.getStatusMessage();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    public void testIsSuccessFromIdam() {

        IdamRegistrationInfo sut = new IdamRegistrationInfo(responseEntityMock);
        Boolean actual = sut.isSuccessFromIdam();

        assertThat(actual).isTrue();
    }

    @Test
    public void testIsDuplicateUser() {
        IdamRegistrationInfo sut = new IdamRegistrationInfo(status(CONFLICT).build());
        assertThat(sut.isDuplicateUser()).isTrue();
    }

    @Test
    public void getIdamRegistrationResponse() {
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(ACCEPTED);
    }

}