package uk.gov.hmcts.reform.userprofileapi.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class TestFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(TestFilter.class);

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        LOG.info(String.format(":Authorization Header:: %S", ((HttpServletRequest) request).getHeader(
                "Authorization")));
        LOG.info(String.format(":ServiceAuthorization Header:: %S", ((HttpServletRequest) request).getHeader(
                "ServiceAuthorization")));
        chain.doFilter(request, response);
    }
}
