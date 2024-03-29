package uk.gov.hmcts.reform.userprofileapi.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class UnauthorizedExceptionTest {


    @Test
    void test_handle_feign_exception() {
        UnauthorizedException unAuthorizedException =
                new UnauthorizedException("User is not authorized", new Exception());
        assertThat(unAuthorizedException).isNotNull();
        assertEquals("User is not authorized", unAuthorizedException.getMessage());
    }
}