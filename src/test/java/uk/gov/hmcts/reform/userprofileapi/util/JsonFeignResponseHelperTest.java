package uk.gov.hmcts.reform.userprofileapi.util;

import feign.Request;
import feign.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;

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

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;
import static uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper.getResponseMapperClass;

@SuppressWarnings("unchecked")
class JsonFeignResponseHelperTest {

    @Test
    void test_Decode_with_gzip() {
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
    void test_Decode_without_gzip_and_code_not_200_and_null() {
        Request request = mock(Request.class);

        Map<String, Collection<String>> header = new HashMap<>();
        Collection<String> list = new ArrayList<>();
        header.put("content-encoding", list);

        Response response = Response.builder().status(200).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional =
                JsonFeignResponseHelper.decode(response, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional).isNotEmpty();

        Response response1 = Response.builder().status(400).reason("OK").headers(header)
                .body("{\"idamId\": 1}", UTF_8).request(request).build();

        Optional<UserProfileCreationResponse> createUserProfileResponseOptional1 =
                JsonFeignResponseHelper.decode(response1, Optional.of(UserProfileCreationResponse.class));

        assertThat(createUserProfileResponseOptional1).isNotEmpty();
    }

    @Test
    void test_Decode_for_non_gzip_with_decode_fails_with_ioException() {
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
    void test_Decode_for_gzip_with_decode_fails_with_ioException() {

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
    void test_convertHeaders() {
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
    void test_toResponseEntity_with_payload_not_empty() {

        ResponseEntity<UserProfileCreationResponse> profileCreationResponseResponseEntity =
                JsonFeignResponseHelper.toResponseEntity(getResponse(200, true),
                        Optional.of(UserProfileCreationResponse.class));

        assertThat(profileCreationResponseResponseEntity).isNotNull();
        assertThat(profileCreationResponseResponseEntity.getStatusCode().value()).isEqualTo(200);
        assertThat(profileCreationResponseResponseEntity.getHeaders()).isNotEmpty();
        assertThat(Objects.requireNonNull(profileCreationResponseResponseEntity.getBody()).getIdamId()).isEqualTo("1");
    }

    @Test
    void test_privateConstructor() throws Exception {
        Constructor<JsonFeignResponseHelper> constructor = JsonFeignResponseHelper.class.getDeclaredConstructor();
        Assertions.assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        constructor.newInstance((Object[]) null);
    }

    @Test
    void test_getResponseMapperClass_when_response_success_and_expected_mapper_class_is_passed() {
        Optional<?> optionalObj = getResponseMapperClass(getResponse(200, false),
                ErrorResponse.class);
        Assertions.assertTrue(optionalObj.isPresent());
        assertThat(optionalObj).isExactlyInstanceOf(Optional.class);
    }

    @Test
    void test_getResponseMapperClass_when_response_success_and_expected_mapper_class_is_passed_empty() {
        Optional<?> optionalObj = getResponseMapperClass(getResponse(200, false), null);
        Assertions.assertFalse(optionalObj.isPresent());
    }

    @Test
    void test_getResponseMapperClass_when_response_failure() {
        Optional<Object> optionalObj = getResponseMapperClass(getResponse(400, false),
                IdamErrorResponse.class);
        Assertions.assertTrue(optionalObj.isPresent());
        assertThat(optionalObj).contains(IdamErrorResponse.class);
    }

    @Test
    void test_getResponseMapperClass_when_response_failure_with_error_code_100() {
        Optional<IdamErrorResponse> optionalObj = getResponseMapperClass(getResponse(100, false),
                null);
        Assertions.assertTrue(optionalObj.isPresent());
    }

    Response getResponse(int statusCode, boolean isMultiHeader) {

        return Response.builder().status(statusCode).reason("OK").headers(getHeader(isMultiHeader))
                .body("{\"idamId\": 1}", UTF_8).request(mock(Request.class)).build();
    }

    private Map<String, Collection<String>> getHeader(boolean isMultiheader) {
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
