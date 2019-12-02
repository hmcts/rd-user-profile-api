package uk.gov.hmcts.reform.userprofileapi.controller;

import java.io.IOException;
import java.util.Objects;

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


        String param1 =  ((HttpServletRequest) request).getHeader(
                "Authorization");
        String param2 =  ((HttpServletRequest) request).getHeader(
                "ServiceAuthorization");

        param1 = Objects.isNull(param1) ? "" : param1.replaceAll("[\n|\r|\t]", "_");
        param2 = Objects.isNull(param2) ? "" : param2.replaceAll("[\n|\r|\t]", "_");

        LOG.info(String.format(":Authorization Header:: %S", param1));
        LOG.info(String.format(":ServiceAuthorization Header:: %S", param2));
        chain.doFilter(request, response);
    }
}
