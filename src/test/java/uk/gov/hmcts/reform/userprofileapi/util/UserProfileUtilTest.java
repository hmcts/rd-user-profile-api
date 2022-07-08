package uk.gov.hmcts.reform.userprofileapi.util;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import uk.gov.hmcts.reform.userprofileapi.exception.IdamServiceException;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserProfileUtilTest {

    UserProfileUtil userProfileUtil = mock(UserProfileUtil.class);

    @Test
    void testGetUserEmailFromHeader() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String email = "test@test.com";

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn("test@test.com");

        assertThat(email).isEqualTo(UserProfileUtil.getUserEmailFromHeader());

        verify(httpRequest, times(2)).getHeader("UserEmail");
    }

    @Test
    void testIdam5xxxErrorResponse() {
        Throwable exception = assertThrows(IdamServiceException.class, () -> {
            UserProfileUtil.idam5xxxErrorResponse(
                    "18 Unknown error from Idam", HttpStatus.BAD_GATEWAY);
        });
        assertEquals("18 Unknown error from Idam", exception.getMessage());
    }
}
