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


        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest((HttpServletRequest) request);
        mutableRequest.putHeader("Authorization", "Bearer eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiS0N4QmRlaHNIVUY2OTc4U2l6dklTRXhjWDBFPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJhYmhpaml0QGdtYWlsLmNvbSIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY"
                + "2tpbmdJZCI6IjYzNTE2NzQxLTgzMTMtNDdkMS1iMzc4LTBlMTQ1ZmU2ZWVmZCIsImlzcyI6Imh0dHBzOi8vZm9yZ2Vyb2NrLWFtLnNlcnZpY2UuY29yZS1jb21wdXRlLWlkYW0tZGVtby5pbnRlcm5hbDo4NDQzL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b"
                + "2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI2MmFkY2QwMC04Yjg4LTQ2MjgtOGM1Zi1hODgwYjA2MGVmYTQiLCJhdWQiOiJyZC1wcm9mZXNzaW9uYWwtYXBpIiwibmJmIjoxNTc1MzI0MDM1LCJncmFudF90eXBlIjoicGFzc3dvcmQiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsIm1hbmFnZS11c2VyIiwic2VhcmNoLXVzZXIiXSwiYXV0aF90aW1lIjoxNTc1MzI0MDM1LCJyZWFsbSI6Ii9obWN0cyIsImV4cCI6MTU3NTM1MjgzNSwiaWF0IjoxNTc1MzI0MDM1LCJleHBpcmVzX2luIjoyODgwMCwianRpIjoiZDc5MTU1NTEtMTVlNy00ODcxLTg4ZWMtMGRhOWU5NzI0NzhjIn0.H8GnUIjfuWJ9xj0vQeivZGyuWZidrJj6DCNXCKRe7ynvaPnt5j56rRwRNP_8jLOvdJl1gGYdytUCEnS3E4YSVgkDxSApzsv7KchkXAgy3V3v_YO0wl0vHIMvuWDCjgIpsCqmx5aW2nsYZBZzQA4IU0ezuaQLKct7uAx9G_exDADnqBy5_muhpN-ujGUhtKPfuHlHWQ7rdyi4pilC_FlV-_u_6x0hrCQ8eK96bHtxfyGfvyno19nuQUWO55Y_kgFRSg82GURfqpjPm7XJnehLN0BnPm0L4U0_NDz2MRlKz87lu8NnOy9AYJ-2UtyFTbFBom1VYiPKkm4ythpPkL5ciA");
        mutableRequest.putHeader("ServiceAuthorization", "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJyZF91c2VyX3Byb2ZpbGVfYXBpIiwiZXhwIjoxNTc1MzM4NTU5fQ.fDKn8pwIdzil2tPLmIXEsejdUXzKHmg9Xgi67ZwlwA1siflpzDbTiigKj46MD-bYs7eIwpo0sNa5jyeKculx-g");

        String param1 =  ((HttpServletRequest) request).getHeader(
                "Authorization");
        String param2 =  ((HttpServletRequest) request).getHeader(
                "ServiceAuthorization");

        param1 = Objects.isNull(param1) ? "" : param1.replaceAll("[\n|\r|\t]", "_");
        param2 = Objects.isNull(param2) ? "" : param2.replaceAll("[\n|\r|\t]", "_");

        LOG.info(String.format(":Authorization Header:: %S", param1));
        LOG.info(String.format(":ServiceAuthorization Header:: %S", param2));
        chain.doFilter(mutableRequest, response);
    }
}
