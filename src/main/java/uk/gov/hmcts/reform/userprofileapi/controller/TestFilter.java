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
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

@Component
@Order(1)
public class TestFilter extends GenericFilterBean {

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

        LOG.info(String.format(":Authorization Header from swagger:: %S", param1));
        LOG.info(String.format(":ServiceAuthorization from swagger:: %S", param2));

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest) request);
        mutableRequest.putHeader("Authorization", "eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiS0N4QmRlaHNIVUY2OTc4U2l6dklTRXhjWDBFPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhYmhpaml0QGdtYWlsLmNvbSIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6ImQzNTc4ODA1LWEzY2EtNGZhOC1hZDAyLWRjMzcyOTg0ZmRjZCIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tZGVtby5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI2YWRlMTU3MS02Yjc5LTRlNGYtOTZjYS0yNGU5NTc2MTg5ZGQiLCJhdWQiOiJyZC1wcm9mZXNzaW9uYWwtYXBpIiwibmJmIjoxNTc1MzY1NDI0LCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsIm1hbmFnZS11c2VyIiwic2VhcmNoLXVzZXIiXSwiYXV0aF90aW1lIjoxNTc1MzY1NDI0LCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU3NTM5NDIyNCwiaWF0IjoxNTc1MzY1NDI0LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiN2U2MGIwMDItN2U5ZC00YWZhLWE2YWMtZWY5OTBhNjI2OTM0In0.YRzi61IELcwDoUUtV1M4gxJtGAWdQyCwZLNuUutjBu0tQNJervtqMgmrDJHHZ58I07QIHqbxlGLpsOlK1PsSkNs9DIyITyltpt-88rOzVjhGop4xVVYN77JfqJibwjdLmEEk_6xmkeB_I6ybxcLkb9lD4dmilOUTnGfHCsoOTehANEccMeOdypUemX4IyOdsgh-ru7_LPJkWOmkaz4lQz7pZPqEwHNPphznRNHrnpHcySrDBJV-ur09SyQ52as1zFbBnYFOW_b5WHnmUZMh7iILPdll7G40ZbN0z7hKT7tv4jU1Gjt-yHN5WB9aTvcxdtGdW2b4AFJQNh-b-pTI1Eg");
        mutableRequest.putHeader("ServiceAuthorization", "hbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF91c2VyX3Byb2ZpbGVfYXBpIiwiZXhwIjoxNTc1Mzc5OTE2fQ.gIN7QFT53q9OEb-PksPflh2gX83ZwgSqf_mrcv4OwV6C2siZauh71DosEjkYa9NJJ4FKO93WsAapWX6PWZ4X-g");

        param1 =  ((HttpServletRequest) mutableRequest).getHeader(
                "Authorization");
        param2 =  ((HttpServletRequest) mutableRequest).getHeader(
                "ServiceAuthorization");

        param1 = Objects.isNull(param1) ? "" : param1.replaceAll("[\n|\r|\t]", "_");
        param2 = Objects.isNull(param2) ? "" : param2.replaceAll("[\n|\r|\t]", "_");

        LOG.info(String.format(":Authorization Header:: %S", param1));
        LOG.info(String.format(":ServiceAuthorization Header:: %S", param2));
        chain.doFilter(mutableRequest, response);
    }
}
