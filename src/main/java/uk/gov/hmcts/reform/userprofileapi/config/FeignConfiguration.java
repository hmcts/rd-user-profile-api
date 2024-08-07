package uk.gov.hmcts.reform.userprofileapi.config;

import feign.codec.Encoder;
import feign.form.spring.SpringFormEncoder;
import feign.okhttp.OkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.cloud.openfeign.support.SpringEncoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;



@Configuration
@Slf4j
public class FeignConfiguration {

    @Bean
    @Primary
    public Encoder feignFormEncoder(
        ObjectFactory<HttpMessageConverters> messageConverters
    ) {
        return new SpringFormEncoder(new SpringEncoder(messageConverters));
    }

    @Bean
    public OkHttpClient client() {
        return new OkHttpClient();
    }
}
