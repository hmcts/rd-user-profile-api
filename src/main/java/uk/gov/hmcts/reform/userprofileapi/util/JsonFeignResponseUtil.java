package uk.gov.hmcts.reform.userprofileapi.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;


@SuppressWarnings("unchecked")
@Slf4j
public class JsonFeignResponseUtil {
    private static final ObjectMapper json = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private JsonFeignResponseUtil() {

    }

    public static Optional<Object> decode(Response response,  Object clazz) {
        try {
            return Optional.of(json.readValue(response.body().asReader(Charset.defaultCharset()),
                    (Class<Object>)clazz));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public static ResponseEntity<Object> toResponseEntity(Response response, Object  clazz) {
        Optional<Object> payload = decode(response, clazz);

        return new ResponseEntity<>(
                payload.orElse(null),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static ResponseEntity<Object>  toResponseEntity(Response response, TypeReference<?>  reference) {
        Optional<Object> payload = Optional.empty();

        try {
            payload = Optional.of(json.readValue(response.body().asReader(Charset.defaultCharset()), reference));

        } catch (IOException ex) {

            log.error("error while reading the body", ex);
        }

        return new ResponseEntity<>(
                payload.orElse(null),
                convertHeaders(response.headers()),
                HttpStatus.valueOf(response.status()));
    }

    public static MultiValueMap<String, String> convertHeaders(Map<String, Collection<String>> responseHeaders) {
        HttpHeaders responseEntityHeaders = new HttpHeaders();
        responseHeaders.entrySet().stream().forEach(e ->
                responseEntityHeaders.put(e.getKey(), new ArrayList<>(e.getValue())));
        return responseEntityHeaders;
    }
}


