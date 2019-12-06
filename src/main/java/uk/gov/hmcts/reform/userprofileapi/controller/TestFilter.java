package uk.gov.hmcts.reform.userprofileapi.controller;

import java.io.IOException;
import java.util.Objects;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
@Order(1)
public class TestFilter extends GenericFilterBean {

    private static final Logger LOG = LoggerFactory.getLogger(TestFilter.class);

    @Value("${idam.s2s-auth.totp_secret}")
    String totpSecret;

    @Value("${idam.s2s-auth.microservice}")
    String s2sAuthMicroservicename;

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

        LOG.info(String.format("::totpSecret:::: %S", totpSecret));
        LOG.info(String.format("::s2sAuthMicroservicename:: %S", s2sAuthMicroservicename));


        LOG.info(String.format(":Authorization Header from swagger:: %S", param1));
        LOG.info(String.format(":Authorization Header from swagger:: %S", param2));


        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest) request);

        chain.doFilter(mutableRequest, response);
    }
}
