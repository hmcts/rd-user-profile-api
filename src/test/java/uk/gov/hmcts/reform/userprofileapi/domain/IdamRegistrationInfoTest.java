package uk.gov.hmcts.reform.userprofileapi.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class IdamRegistrationInfoTest {

    private IdamRegistrationInfo sut;

    private Optional<ResponseEntity> responseEntityMockOptional;

    private HttpStatus httpStatusMock;


    @Before
    public void setUp() throws Exception {
        httpStatusMock = Mockito.mock(HttpStatus.class);
        responseEntityMockOptional = Optional.ofNullable(Mockito.mock(ResponseEntity.class));

        sut = new IdamRegistrationInfo(httpStatusMock, responseEntityMockOptional);
    }

    @Test
    public void test_OneArgConstructor() {
        final HttpStatus inputMessage = UNAUTHORIZED;
        final String expectMessage = resolveStatusAndReturnMessage(inputMessage);

        when(httpStatusMock.toString()).thenReturn(expectMessage);

        IdamRegistrationInfo sut = new IdamRegistrationInfo(inputMessage);
        String actualMessage = sut.getStatusMessage();

        assertThat(actualMessage).isNotNull();
        assertThat(actualMessage).isEqualTo(expectMessage);
    }

    @Test
    public void test_isSuccessFromIdam() {
        when(httpStatusMock.is2xxSuccessful()).thenReturn(true);

        IdamRegistrationInfo sut = new IdamRegistrationInfo(httpStatusMock);
        Boolean actual = sut.isSuccessFromIdam();

        assertThat(actual).isTrue();
    }

    @Test
    public void test_isDuplicateUser() {
        IdamRegistrationInfo sut = new IdamRegistrationInfo(CONFLICT);

        assertThat(sut.isDuplicateUser()).isTrue();

        sut = new IdamRegistrationInfo(ACCEPTED);

        assertThat(sut.isDuplicateUser()).isFalse();
    }

    @Test
    public void test_getIdamRegistrationResponse() {
        assertThat(sut.getIdamRegistrationResponse()).isEqualTo(httpStatusMock);
    }

    @Test
    public void test_getResponse() {
        assertThat(sut.getResponse()).isEqualTo(responseEntityMockOptional.get());
    }
}