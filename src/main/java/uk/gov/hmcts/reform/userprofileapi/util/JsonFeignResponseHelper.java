package uk.gov.hmcts.reform.userprofileapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.zip.GZIPInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;


@Slf4j
public class JsonFeignResponseHelper {
    private static final ObjectMapper json = new ObjectMapper();

    private JsonFeignResponseHelper() { }

    public static <U> ResponseEntity<U> toResponseEntity(Response response, Optional<Class<U>> classOpt) {
        Optional<U> payload = decode(response, classOpt);
        MultiValueMap<String, String> headers = convertHeaders(response.headers());
        HttpStatus httpStatus = HttpStatus.valueOf(response.status());
        return (payload.isPresent())
                ? new ResponseEntity<U>(payload.orElse(null), headers, httpStatus)
                : new ResponseEntity<U>(headers, httpStatus);
    }

    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        MultiValueMap<String, String> responseEntityHeaders = new LinkedMultiValueMap<>();
        responseHeaders.entrySet().stream().forEach(e ->
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue())));
        return responseEntityHeaders;
    }

    public static <T> Optional<T> decode(Response response, Optional<Class<T>> clazz) {
        Optional<T> result = Optional.empty();
        if (isStatusCodeSuccessful(response.status()) && clazz.isPresent()) {
            try {
                Optional<Collection<String>> encodings = Optional.ofNullable(response.headers().get("content-encoding"));
                result = Optional.of((encodings.isPresent() && encodings.get().contains("gzip"))
                        ? json.readValue(new GZIPInputStream(new BufferedInputStream(response.body().asInputStream())), clazz.get())
                        : json.readValue(response.body().asReader(), clazz.get()));
            } catch (IOException e) {
                log.warn("Error could not decode!");
            }
        }
        return result;
    }

    public static boolean isStatusCodeSuccessful(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

}
