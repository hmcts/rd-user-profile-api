package uk.gov.hmcts.reform.userprofileapi;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.retry.annotation.EnableRetry;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.idam.client.IdamApi;

@EnableJpaAuditing
@EnableJpaRepositories
@EnableRetry
@EnableCaching
@ImportAutoConfiguration({FeignAutoConfiguration.class})
@SpringBootApplication(scanBasePackages = {"uk.gov.hmcts.reform.idam", "uk.gov.hmcts.reform.userprofileapi"})
@EnableFeignClients(basePackages = {"uk.gov.hmcts.reform.userprofileapi"},
        basePackageClasses = {IdamApi.class, ServiceAuthorisationApi.class})
@SuppressWarnings("HideUtilityClassConstructor") // Spring needs a constructor, it's not a utility class
public class Application {

    public static void main(final String[] args) {
        ApplicationInsights.attach();
        SpringApplication.run(Application.class, args);
    }
}
