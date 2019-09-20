package uk.gov.hmcts.reform.userprofileapi.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import feign.Request;
import feign.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;

public class JsonFeignResponseHelperTest {

    @Test
    public void testDecode_with_gzip() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"gzip",  ""}));
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();
        Optional<CreateUserProfileResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(CreateUserProfileResponse.class));
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void testDecode_without_gzip() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();
        Optional<CreateUserProfileResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(CreateUserProfileResponse.class));
        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void testDecode_with_status_code_not_200() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(400).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();
        Optional<CreateUserProfileResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(CreateUserProfileResponse.class));
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void testDecode_with_class_to_decode_is_passed_null() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(400).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();
        Optional<CreateUserProfileResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.ofNullable(CreateUserProfileResponse.class));
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void testDecode_for_non_gzip_with_decode_fails_with_ioException() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock).request(request).build();
        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader()).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Optional<CreateUserProfileResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(CreateUserProfileResponse.class));
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void testDecode_for_gzip_with_decode_fails_with_ioException() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"gzip",  ""}));
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock).request(request).build();
        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader()).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Optional<CreateUserProfileResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(CreateUserProfileResponse.class));
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void test_isStatusCodeSuccessful() {
        assertThat(JsonFeignResponseHelper.isStatusCodeSuccessful(200)).isTrue();
        assertThat(JsonFeignResponseHelper.isStatusCodeSuccessful(201)).isTrue();
        assertThat(JsonFeignResponseHelper.isStatusCodeSuccessful(400)).isFalse();
        assertThat(JsonFeignResponseHelper.isStatusCodeSuccessful(401)).isFalse();
    }

    @Test
    public void test_convertHeaders() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"gzip",  ""}));
        header.put("content-encoding", list);
        MultiValueMap<String, String> responseHeader = JsonFeignResponseHelper.convertHeaders(header);
        assertThat(responseHeader).isNotEmpty();

        Collection<String> emptylist = new ArrayList<>();
        header.put("content-encoding", emptylist);
        MultiValueMap<String, String> responseHeader1 = JsonFeignResponseHelper.convertHeaders(header);
        assertThat(responseHeader1.get("content-encoding")).isEmpty();

    }

    @Test
    public void test_toResponseEntity_with_payload_not_empty() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>(Arrays.asList(new String[]{"a", "b"}));
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();
        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, Optional.of(CreateUserProfileResponse.class));
        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders()).isNotEmpty();
        assertThat(((CreateUserProfileResponse)entity.getBody()).getIdamId()).isEqualTo("1");
    }
}
