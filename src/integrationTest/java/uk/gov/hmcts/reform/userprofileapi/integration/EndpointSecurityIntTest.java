package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@Transactional
class EndpointSecurityIntTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void should_allow_unauthenticated_requests_to_welcome_and_return_200_response_code() throws Exception {

        MvcResult result = mockMvc.perform(get("/")
                        .header("Content-Type", APPLICATION_JSON_VALUE)
                        .header("Accepts", APPLICATION_JSON_VALUE)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is(OK.value()))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("Welcome to the User Profile API");
    }

    @Test
    void should_allow_unauthenticated_requests_to_health_check_and_return_200_response_code() throws Exception {
        healthEndpointMock();

        MvcResult result = mockMvc.perform(get("/health")
                        .header("Content-Type", APPLICATION_JSON_VALUE)
                        .header("Accepts", APPLICATION_JSON_VALUE)
                        .contentType(APPLICATION_JSON))
                .andExpect(status().is(OK.value()))
                .andReturn();

        assertThat(result.getResponse().getContentAsString()).contains("UP");


    }
}
