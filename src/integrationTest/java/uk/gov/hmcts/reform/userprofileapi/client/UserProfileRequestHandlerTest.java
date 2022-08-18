package uk.gov.hmcts.reform.userprofileapi.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@Component
@Slf4j
public class UserProfileRequestHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private static final String IDAM_TOKEN = "";

    private static final String JWT_TOKEN = "";

    public static final String COMMON_EMAIL_PATTERN = "@prdfunctestuser.com";

    public MvcResult sendPost(MockMvc mockMvc,
                              String path,
                              String jsonBody,
                              HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(post(path)
                .headers(getMultipleAuthHeaders())
                .content(jsonBody)
                .contentType(APPLICATION_JSON))
                .andReturn();
    }

    public MvcResult sendPost(MockMvc mockMvc,
                              String path,
                              Object body,
                              HttpStatus expectedHttpStatus) throws Exception {

        return sendPost(mockMvc, path, objectMapper.writeValueAsString(body), expectedHttpStatus);

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
        if(clazz.getName().contains("ErrorResponse")  ){
            ErrorResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), ErrorResponse.class);
            response.setStatus(result.getResponse().getStatus());
            return (T) response;
        }
        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public MvcResult sendGet(MockMvc mockMvc,
                             String path,
                             HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(get(path)
                .headers(getMultipleAuthHeaders())
                .contentType(APPLICATION_JSON))
                .andReturn();
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

    public <T> T sendGetFromHeader(MockMvc mockMvc,
                                   String path,
                                   HttpStatus expectedHttpStatus,
                                   Class<T> clazz, String email) throws Exception {

        MvcResult result = sendGetFromHeader(mockMvc, path, expectedHttpStatus, email);
        assertThat(result.getResponse().getContentAsString())
                .as("Expected json content was empty")
                .isNotEmpty();

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public MvcResult sendGetFromHeader(MockMvc mockMvc,
                                       String path,
                                       HttpStatus expectedHttpStatus, String email) throws Exception {
        HttpHeaders httpHeaders = getMultipleAuthHeaders();
        httpHeaders.add("UserEmail", email);
        return mockMvc.perform(get(path)
                .headers(httpHeaders)
                .contentType(APPLICATION_JSON))
                .andReturn();
    }

    public <T> T sendPut(MockMvc mockMvc,
                         String path,
                         Object body,
                         HttpStatus expectedHttpStatus,
                         Class<T> clazz) throws Exception {

        MvcResult result = sendPut(mockMvc, path, body, expectedHttpStatus);
        assertThat(result.getResponse().getContentAsString())
                .as("Expected json content was empty")
                .isNotEmpty();

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public MvcResult sendPut(MockMvc mockMvc,
                             String path,
                             Object body,
                             HttpStatus expectedHttpStatus) throws Exception {

        return sendPut(mockMvc, path, objectMapper.writeValueAsString(body), expectedHttpStatus);
    }

    public MvcResult sendPut(MockMvc mockMvc,
                             String path,
                             String jsonBody,
                             HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(put(path)
                .headers(getMultipleAuthHeaders())
                .content(jsonBody)
                .contentType(APPLICATION_JSON))
                .andReturn();
    }

    public <T> T sendDelete(MockMvc mockMvc,
                            String path,
                            Object body,
                            HttpStatus expectedHttpStatus,
                            Class<T> clazz) throws Exception {

        MvcResult result = mockMvc.perform(delete(path)
                .headers(getMultipleAuthHeaders())
                .content(objectMapper.writeValueAsString(body))
                .contentType(APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .as("Expected json content was empty")
                .isNotEmpty();

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public <T> T sendDeleteWithoutBody(MockMvc mockMvc,
                                       String path,
                                       HttpStatus expectedHttpStatus,
                                       Class<T> clazz) throws Exception {

        MvcResult result = mockMvc.perform(delete(path)
                .headers(getMultipleAuthHeaders())
                .contentType(APPLICATION_JSON))
                .andReturn();

        assertThat(result.getResponse().getContentAsString())
                .as("Expected json content was empty")
                .isNotEmpty();

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    private HttpHeaders getMultipleAuthHeaders() {

        log.info("JWT TOKEN::" + JWT_TOKEN);
        log.info("IDAM_TOKEN::" + IDAM_TOKEN);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);

        headers.add("ServiceAuthorization", JWT_TOKEN);
        headers.add("Authorization", IDAM_TOKEN);

        return headers;
    }

}
