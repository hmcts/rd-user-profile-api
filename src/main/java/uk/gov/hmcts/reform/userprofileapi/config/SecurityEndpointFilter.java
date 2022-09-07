package uk.gov.hmcts.reform.userprofileapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.userprofileapi.exception.UnauthorizedException;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Configuration
@Slf4j
public class SecurityEndpointFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            log.debug("Inside Security EndPoint Filter" + request.getContextPath() + request.getPathInfo()
                    + request.getRequestURI() + request.getQueryString() + request.getRequestedSessionId());
            filterChain.doFilter(request, response);
            log.debug("After Security EndPoint Filter" + response);
        } catch (Exception e) {
            Throwable throwable = e.getCause();
            log.error("caught an exception in filter"
                    + throwable);
            if (e instanceof UnauthorizedException) {
                log.error("Authorisation exception", e);
                response.sendError(HttpStatus.FORBIDDEN.value(), "Access Denied");
                return;
            }
            throw e;
        }
    }
}

