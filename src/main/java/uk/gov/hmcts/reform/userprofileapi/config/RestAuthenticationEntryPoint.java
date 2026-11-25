package uk.gov.hmcts.reform.userprofileapi.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;

import java.io.IOException;
import java.time.LocalDateTime;

@Component("restAuthenticationEntryPoint")
@Slf4j
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final JsonMapper jsonMapper;

    public RestAuthenticationEntryPoint(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void commence(jakarta.servlet.http.HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, jakarta.servlet.ServletException {

        ErrorResponse errorResponse = ErrorResponse.builder()
                .errorMessage("Authentication Exception")
                .errorDescription(authException.getMessage())
                .timeStamp(LocalDateTime.now().toString())
                .build();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        String errorMessage = jsonMapper.writeValueAsString(errorResponse);
        response.setHeader("UnAuthorized-Token-Error", errorMessage);
        log.error(errorMessage);
        log.debug("Inside RestAuthenticationEntryPoint" + errorMessage);
    }
}