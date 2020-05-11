package uk.gov.hmcts.reform.userprofileapi.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper.getResponseMapperClass;

import feign.Request;
import feign.Response;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;

public class JsonFeignResponseHelperTest {

    @Test
    public void testDecode_with_gzip() {
        Request request = mock(Request.class);

        Collection<String> list = Arrays.asList("gzip", "");

        Map<String, Collection<String>> header = new HashMap<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void testDecode_without_gzip() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void testDecode_with_status_code_not_200() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(400).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void testDecode_with_class_to_decode_is_passed_null() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(400).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void testDecode_for_non_gzip_with_decode_fails_with_ioException() {
        Request request = mock(Request.class);
        Response.Body bodyMock = mock(Response.Body.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock).request(request).build();
        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader()).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));
        
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void testDecode_for_gzip_with_decode_fails_with_ioException() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = Arrays.asList("gzip", "");
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
        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void test_convertHeaders() {
        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = Arrays.asList("gzip", "");
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
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = Arrays.asList("a", "b");
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body("{\"idamId\": 1}", UTF_8).request(request).build();

        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders()).isNotEmpty();
        assertThat(((UserProfileCreationResponse) Objects.requireNonNull(entity.getBody())).getIdamId()).isEqualTo("1");
    }

    @Test
    public void privateConstructorTest() throws Exception {
        Constructor<JsonFeignResponseHelper> constructor = JsonFeignResponseHelper.class.getDeclaredConstructor();
        assertFalse(constructor.isAccessible());
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_getResponseMapperClass_when_response_success_and_expected_mapper_class_is_passed() {
        Response response = Response.builder().status(200).reason("OK").body("{\"idamId\": 1}", UTF_8).request(mock(Request.class)).build();
        Optional<ErrorResponse> optionalObj = getResponseMapperClass(response, Optional.of(ErrorResponse.class));
        assertTrue(optionalObj.isPresent());
        assertThat(optionalObj.get()).isEqualTo(ErrorResponse.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void test_getResponseMapperClass_when_response_success_and_expected_mapper_class_is_passed_empty() {
        Response response = Response.builder().status(200).reason("OK").body("{\"idamId\": 1}", UTF_8).request(mock(Request.class)).build();
        Optional optionalObj = getResponseMapperClass(response, Optional.empty());
        assertFalse(optionalObj.isPresent());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getResponseMapperClass_when_response_failure() {
        Response response = Response.builder().status(400).reason("OK").body("{\"idamId\": 1}", UTF_8).request(mock(Request.class)).build();
        Optional optionalObj = getResponseMapperClass(response, Optional.of(IdamErrorResponse.class));
        assertTrue(optionalObj.isPresent());
        assertThat(optionalObj.get()).isEqualTo(IdamErrorResponse.class);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void test_getResponseMapperClass_when_response_failure_with_error_code_100() {
        Response response = Response.builder().status(100).reason("OK").body("{\"idamId\": 1}", UTF_8).request(mock(Request.class)).build();
        Optional<IdamErrorResponse> optionalObj = getResponseMapperClass(response, Optional.empty());
        assertTrue(optionalObj.isPresent());
        assertThat(optionalObj.get()).isEqualTo(IdamErrorResponse.class);

    }
}
