package uk.gov.hmcts.reform.userprofileapi.controller.response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver.resolveStatusAndReturnMessage;

class RoleAdditionResponseTest {

    private final HttpStatus okStatus = HttpStatus.OK;
    private RoleAdditionResponse sut;

    @BeforeEach
    public void setUp() {
        sut = new RoleAdditionResponse(ResponseEntity.status(okStatus).build());
    }

    @Test
    void test_AddRoleResponseNoArgConstructor() {
        sut = new RoleAdditionResponse();
        assertThat(sut.getIdamMessage()).isNull();
        assertThat(sut.getIdamStatusCode()).isNull();
    }

    @Test
    void test_AddRoleResponse() {
        assertThat(sut.getIdamMessage()).isEqualTo(resolveStatusAndReturnMessage(okStatus));
        assertThat(sut.getIdamStatusCode()).isEqualTo(String.valueOf(okStatus.value()));
    }

    @Test
    void test_AddRoleResponseSetter() {
        final HttpStatus errorStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        final String expectedMessage = resolveStatusAndReturnMessage(errorStatus);
        final String expectedCode = String.valueOf(errorStatus.value());
        sut.setIdamMessage(expectedMessage);
        sut.setIdamStatusCode(expectedCode);

        assertThat(sut.getIdamMessage()).isEqualTo(expectedMessage);
        assertThat(sut.getIdamStatusCode()).isEqualTo(expectedCode);
    }
}
