package uk.gov.hmcts.reform.userprofileapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(JacksonAutoConfiguration.class, JacksonConfiguration.class);

    @Test
    void jsonMapperStartsWithoutABeanCycle() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(JsonMapper.class);
            JsonMapper mapper = context.getBean(JsonMapper.class);
            assertThat(mapper.readTree("{\"unexpected\":true}").isObject()).isTrue();
        });
    }
}
