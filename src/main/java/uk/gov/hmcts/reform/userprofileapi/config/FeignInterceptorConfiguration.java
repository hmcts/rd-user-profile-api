package uk.gov.hmcts.reform.userprofileapi.config;

import feign.Feign;
import feign.RequestInterceptor;
import feign.Retryer;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
public class FeignInterceptorConfiguration {

    @Value("${loggingComponentName}")
    private String loggingComponentName;

    @Bean
    public RequestInterceptor requestInterceptor(FeignHeaderConfig config) {
        return requestTemplate -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        String name = headerNames.nextElement();
                        String value = request.getHeader(name);
                        if (config.getHeaders().contains(name.toLowerCase())) {
                            requestTemplate.header(name, value);

                        }
                    }
                } else {

                    log.warn("{}:: {} {}",
                            loggingComponentName, "FeignHeadConfiguration", "Failed to get request header!");

                }
            }
        };
    }

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public Feign.Builder feignBuilder(Retryer r) {
        return Feign.builder().retryer(r)
                .client(new feign.okhttp.OkHttpClient());
    }

}
