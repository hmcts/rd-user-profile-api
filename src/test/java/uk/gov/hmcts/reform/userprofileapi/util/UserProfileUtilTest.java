package uk.gov.hmcts.reform.userprofileapi.util;

import org.junit.Test;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserProfileUtilTest {

    UserProfileUtil userProfileUtil = mock(UserProfileUtil.class);

    @Test
    public void testGetUserEmailFromHeader() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        String email = "adil@praveen.com";

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(httpRequest));
        when(httpRequest.getHeader(anyString())).thenReturn("adil@praveen.com");

        assertThat(email).isEqualTo(userProfileUtil.getUserEmailFromHeader());

        verify(httpRequest, times(2)).getHeader("UserEmail");
    }
}