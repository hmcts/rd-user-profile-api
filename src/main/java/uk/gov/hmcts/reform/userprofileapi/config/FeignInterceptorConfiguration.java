package uk.gov.hmcts.reform.userprofileapi.config;

import feign.Feign;
import feign.RequestInterceptor;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import feign.Retryer;
import feign.httpclient.ApacheHttpClient;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.apache.http.HttpHost;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@Slf4j
public class FeignInterceptorConfiguration {

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
                    log.warn("FeignHeadConfiguration", "Failed to get request header!");
                }
            }
        };
    }

/*    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public  feignBuilder(Retryer r) {
        OkHttpClient client = new OkHttpClient();
        HttpHost proxyHost = new HttpHost("localhost");
        HttpRoutePlanner routePlanner = new DefaultProxyRoutePlanner(proxyHost);
        HttpClientBuilder clientBuilder = HttpClients.custom();

        clientBuilder = clientBuilder.setRoutePlanner(routePlanner);
        return clientBuilder.build();

       return Feign.builder().retryer(r)

                .client();
    }*/

    @Bean
    @Scope("prototype")
    @ConditionalOnMissingBean
    public Feign.Builder feignBuilder(Retryer r) {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxyout.reform.hmcts.net", 8080));
        OkHttpClient okHttpClient = new OkHttpClient.Builder().proxy(proxy).build();
        return Feign.builder().retryer(r)
                .client(new feign.okhttp.OkHttpClient());
    }

}
