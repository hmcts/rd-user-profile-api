package uk.gov.hmcts.reform.userprofileapi.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.cfg.ConstructorDetector;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Jackson 3 mapper settings shared by MVC, Feign, IDAM response decoding, and tests.
 * Annotation-package {@code com.fasterxml.jackson.annotation} metadata is honoured.
 * Jackson 2 databind annotations are not.
 */
public final class ApplicationJsonMapper {

    private ApplicationJsonMapper() {
    }

    public static JsonMapper.Builder configure(JsonMapper.Builder builder) {
        return builder
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                // Jackson 3 fails when a creator omits a primitive. Boot 3 bound those as false.
                .disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
                .enable(EnumFeature.WRITE_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.READ_ENUMS_USING_TO_STRING)
                .enable(EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
                .changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.ALWAYS))
                // Jackson 2 kept the no-arg constructor when one existed. Jackson 3 also
                // treats other visible constructors as creators unless this is disabled.
                .constructorDetector(ConstructorDetector.DEFAULT.withAllowImplicitWithDefaultConstructor(false));
    }

    public static JsonMapper create() {
        return configure(JsonMapper.builder().findAndAddModules()).build();
    }
}
