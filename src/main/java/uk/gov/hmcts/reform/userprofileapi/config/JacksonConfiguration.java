package uk.gov.hmcts.reform.userprofileapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseHelper;
import uk.gov.hmcts.reform.userprofileapi.util.JsonFeignResponseUtil;

/**
 * MVC and Feign use Boot's Jackson 3 {@link JsonMapper}. IDAM {@code feign.Response} bodies are decoded
 * with that same mapper. Do not set {@code spring.http.converters.preferred-json-mapper=jackson2}.
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public JsonMapperBuilderCustomizer userProfileJsonMapperCustomizer() {
        return ApplicationJsonMapper::configure;
    }

    @Autowired
    void bindSharedJsonMapper(JsonMapper jsonMapper) {
        JsonFeignResponseHelper.useMapper(jsonMapper);
        JsonFeignResponseUtil.useMapper(jsonMapper);
    }
}
