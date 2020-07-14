package uk.gov.hmcts.reform.userprofileapi.controller.response;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class RoleAdditionResponseTest {

    private final HttpStatus okStatus = HttpStatus.OK;
    private RoleAdditionResponse sut;

    @Before
    public void setUp() {
        sut = new RoleAdditionResponse(ResponseEntity.status(okStatus).build());
    }

    @Test
    public void test_AddRoleResponseNoArgConstructor() {
        sut = new RoleAdditionResponse();
        assertThat(sut.getIdamMessage()).isNull();
        assertThat(sut.getIdamStatusCode()).isNull();
    }

    @Test
    public void test_AddRoleResponse() {
        assertThat(sut.getIdamMessage()).isEqualTo(resolveStatusAndReturnMessage(okStatus));
        assertThat(sut.getIdamStatusCode()).isEqualTo(String.valueOf(okStatus.value()));
    }

    @Test
    public void test_AddRoleResponseSetter() {
        final HttpStatus errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        final String expectedMessage = resolveStatusAndReturnMessage(errorStatus);
        final String expectedCode = String.valueOf(errorStatus.value());
        sut.setIdamMessage(expectedMessage);
        sut.setIdamStatusCode(expectedCode);

        assertThat(sut.getIdamMessage()).isEqualTo(expectedMessage);
        assertThat(sut.getIdamStatusCode()).isEqualTo(expectedCode);
    }
}