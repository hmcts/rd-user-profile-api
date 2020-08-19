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
import java.lang.reflect.Modifier;
import java.nio.charset.Charset;
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

@SuppressWarnings("unchecked")
public class JsonFeignResponseHelperTest {

    @Test
    public void test_Decode_with_gzip() {
        Request request = mock(Request.class);

        Collection<String> list = Arrays.asList("gzip", "");

        Map<String, Collection<String>> header = new HashMap<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional =
                JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void test_Decode_without_gzip() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional =
                JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void test_Decode_with_status_code_not_200() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(400).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional =
                JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void test_Decode_with_class_to_decode_is_passed_null() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(400).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper
                .decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();
    }

    @Test
    public void test_Decode_for_non_gzip_with_decode_fails_with_ioException() {
        Request request = mock(Request.class);
        Response.Body bodyMock = mock(Response.Body.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock)
                .request(request).build();
        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional = JsonFeignResponseHelper
                .decode(response, Optional.of(UserProfileCreationResponse.class));
        
        assertThat(createUserProfileResponseOptional).isEmpty();
    }

    @Test
    public void test_Decode_for_gzip_with_decode_fails_with_ioException() {

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = Arrays.asList("gzip", "");
        header.put("content-encoding", list);
        Request request = mock(Request.class);
        Response.Body bodyMock = mock(Response.Body.class);
        Response response = Response.builder().status(200).reason("OK").headers(header).body(bodyMock)
                .request(request).build();
        try {
            when(bodyMock.asInputStream()).thenThrow(new IOException());
            when(bodyMock.asReader(Charset.defaultCharset())).thenThrow(new IOException());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Optional<UserProfileCreationResponse> createUserProfileResponseOptional =
                JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));
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

        ResponseEntity entity = JsonFeignResponseHelper.toResponseEntity(getResponse(200, true),
                Optional.of(UserProfileCreationResponse.class));

        assertThat(entity).isNotNull();
        assertThat(entity.getStatusCode().value()).isEqualTo(200);
        assertThat(entity.getHeaders()).isNotEmpty();
        assertThat(((UserProfileCreationResponse) Objects.requireNonNull(entity.getBody())).getIdamId()).isEqualTo("1");
    }

    @Test
    public void test_privateConstructor() throws Exception {
        Constructor<JsonFeignResponseHelper> constructor = JsonFeignResponseHelper.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    public void test_getResponseMapperClass_when_response_success_and_expected_mapper_class_is_passed() {
        Optional optionalObj = getResponseMapperClass(getResponse(200, false),
                ErrorResponse.class);
        assertTrue(optionalObj.isPresent());
        assertThat(optionalObj).isExactlyInstanceOf(Optional.class);
    }

    @Test
    public void test_getResponseMapperClass_when_response_success_and_expected_mapper_class_is_passed_empty() {
        Optional optionalObj = getResponseMapperClass(getResponse(200,false), null);
        assertFalse(optionalObj.isPresent());
    }

    @Test
    public void test_getResponseMapperClass_when_response_failure() {
        Optional optionalObj = getResponseMapperClass(getResponse(400,false),
                IdamErrorResponse.class);
        assertTrue(optionalObj.isPresent());
        assertThat(optionalObj.get()).isEqualTo(IdamErrorResponse.class);
    }

    @Test
    public void test_getResponseMapperClass_when_response_failure_with_error_code_100() {
        Optional<IdamErrorResponse> optionalObj = getResponseMapperClass(getResponse(100, false),
                null);
        assertTrue(optionalObj.isPresent());
    }

    public Response getResponse(int statusCode, boolean isMultiHeader) {

        return Response.builder().status(statusCode).reason("OK").headers(getHeader(isMultiHeader))
                .body("{\"idamId\": 1}", UTF_8).request(mock(Request.class)).build();
    }

    public Map<String, Collection<String>>  getHeader(boolean isMultiheader) {
        Collection<String> list;
        Map<String, Collection<String>> header = new HashMap<>();
        if (isMultiheader) {
            list = Arrays.asList("a", "b");
        } else {
            list = Arrays.asList("gzip", "");
        }
        header.put("content-encoding", list);
        return header;
    }
}
