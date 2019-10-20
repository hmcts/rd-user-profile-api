package uk.gov.hmcts.reform.userprofileapi.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@Component
@Slf4j
public class UserProfileRequestHandlerTest {

    @Autowired
    private ObjectMapper objectMapper;
    private static final String JWT_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    private static final Map<String, String> IDAM_TOKEN = new HashMap<String, String>() {
        {
            put("authToken", "authorization-eyJ0eXAiOiJKV1QiLCJ6aXAiOiJOT05FIiwia2lkIjoiYi9PNk92VnYxK3krV2dySDVVaTlXVGlvTHQwPSIsImFsZyI6IlJTMjU2In0.eyJzdWIiOiJzdXBlci51c2VyQGhtY3RzLm5ldCIsImF1dGhfbGV2ZWwiOjAsImF1ZGl0VHJhY2tpbmdJZCI6IjZiYTdkYTk4LTRjMGYtNDVmNy04ZjFmLWU2N2NlYjllOGI1OCIsImlzcyI6Imh0dHA6Ly9mci1hbTo4MDgwL29wZW5hbS9vYXV0aDIvaG1jdHMiLCJ0b2tlbk5hbWUiOiJhY2Nlc3NfdG9rZW4iLCJ0b2tlbl90eXBlIjoiQmVhcmVyIiwiYXV0aEdyYW50SWQiOiI0NjAzYjVhYS00Y2ZhLTRhNDQtYWQzZC02ZWI0OTI2YjgxNzYiLCJhdWQiOiJteV9yZWZlcmVuY2VfZGF0YV9jbGllbnRfaWQiLCJuYmYiOjE1NTk4OTgxNzMsImdyYW50X3R5cGUiOiJhdXRob3JpemF0aW9uX2NvZGUiLCJzY29wZSI6WyJhY3IiLCJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJjcmVhdGUtdXNlciIsImF1dGhvcml0aWVzIl0sImF1dGhfdGltZSI6MTU1OTg5ODEzNTAwMCwicmVhbG0iOiIvaG1jdHMiLCJleHAiOjE1NTk5MjY5NzMsImlhdCI6MTU1OTg5ODE3MywiZXhwaXJlc19pbiI6Mjg4MDAsImp0aSI6IjgxN2ExNjE0LTVjNzAtNGY4YS05OTI3LWVlYjFlYzJmYWU4NiJ9.RLJyLEKldHeVhQEfSXHhfOpsD_b8dEBff7h0P4nZVLVNzVkNoiPdXYJwBTSUrXl4pyYJXEhdBwkInGp3OfWQKhHcp73_uE6ZXD0eIDZRvCn1Nvi9FZRyRMFQWl1l3Dkn2LxLMq8COh1w4lFfd08aj-VdXZa5xFqQefBeiG_xXBxWkJ-nZcW3tTXU0gUzarGY0xMsFTtyRRilpcup0XwVYhs79xytfbq0WklaMJ-DBTD0gux97KiWBrM8t6_5PUfMDBiMvxKfRNtwGD8gN8Vct9JUgVTj9DAIwg0KPPm1rEETRPszYI2wWvD2lpH2AwUtLBlRDANIkN9SdfiHSETvoQ");
        }
    };

    public MvcResult sendPost(MockMvc mockMvc,
                              String path,
                              String jsonBody,
                              HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(post(path)
            .headers(getMultipleAuthHeaders())
            .content(jsonBody)
            .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().is(expectedHttpStatus.value())).andReturn();
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

        return objectMapper.readValue(result.getResponse().getContentAsString(), clazz);
    }

    public MvcResult sendGet(MockMvc mockMvc,
                             String path,
                             HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(get(path)
            .headers(getMultipleAuthHeaders())
            .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().is(expectedHttpStatus.value()))
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

    public MvcResult sendPut(MockMvc mockMvc,
                              String path,
                              String jsonBody,
                              HttpStatus expectedHttpStatus) throws Exception {

        return mockMvc.perform(put(path)
                .headers(getMultipleAuthHeaders())
                .content(jsonBody)
                .contentType(APPLICATION_JSON_UTF8))
                .andExpect(status().is(expectedHttpStatus.value())).andReturn();
    }

    public void sendPut(MockMvc mockMvc,
                              String path,
                              Object body,
                              HttpStatus expectedHttpStatus) throws Exception {

        sendPut(mockMvc, path, objectMapper.writeValueAsString(body), expectedHttpStatus);
    }

    private HttpHeaders getMultipleAuthHeaders() {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);

        headers.add("ServiceAuthorization", JWT_TOKEN);
        headers.add("Authorization", IDAM_TOKEN.get("authToken"));

        return headers;
    }

}
