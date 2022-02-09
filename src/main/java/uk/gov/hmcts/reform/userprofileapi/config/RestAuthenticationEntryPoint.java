package uk.gov.hmcts.reform.userprofileapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component("restAuthenticationEntryPoint")
@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authenticationException) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage("Authentication Exception")
                .errorDescription(authenticationException.getMessage())
                .timeStamp(LocalDateTime.now().toString())
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        log.error(mapper.writeValueAsString(errorResponse));

    }
}