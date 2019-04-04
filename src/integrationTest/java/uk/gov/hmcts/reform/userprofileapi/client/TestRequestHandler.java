package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Component
public class TestRequestHandler {

    @Autowired
    private ObjectMapper objectMapper;

    public TestRequestHandler() {
    }

    public MvcResult sendPost(MockMvc mockMvc,
                              String path,
                              Object body,
                              HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(post(path)
            .content(objectMapper.writeValueAsString(body))
            .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().is(expectedHttpStatus.value())).andReturn();
    }

    public <T> T sendPost(MockMvc mockMvc,
                          String path,
                          Object body,
                          HttpStatus expectedHttpStatus,
                          Class<T> clazz) throws Exception {

        MvcResult result = sendPost(mockMvc, path, body, expectedHttpStatus);
        assertThat(result.getResponse().getContentAsString())
            .as("Expected json content was empty")
            .isNotEmpty();

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public MvcResult sendGet(MockMvc mockMvc,
                             String path,
                             HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(get(path)
            .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().is(expectedHttpStatus.value())).andReturn();
    }

    public <T> T sendGet(MockMvc mockMvc,
                         String path,
                         HttpStatus expectedHttpStatus,
                         Class<T> clazz) throws Exception {

        MvcResult result = sendGet(mockMvc, path, expectedHttpStatus);
        assertThat(result.getResponse().getContentAsString())
            .as("Expected json content was empty")
            .isNotEmpty();

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }


}
