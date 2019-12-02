package uk.gov.hmcts.reform.userprofileapi.config;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@Slf4j
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final List<String> anonymousPaths = new ArrayList<>();
    private AuthCheckerServiceAndUserFilter authCheckerServiceAndUserFilter;

    @Autowired
    private HttpServletRequest request;

    private static final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

    public SecurityConfiguration(RequestAuthorizer<User> userRequestAuthorizer,

                                 RequestAuthorizer<Service> serviceRequestAuthorizer,

                                 AuthenticationManager authenticationManager) {


        //print all headers
        LOG.info("::Authorization header idam token filter::" + request.getHeader("Authorization"));
        LOG.info("::Authorization header S2S Token s2s token in filter::" + request.getHeader("ServiceAuthorization"));

        //inside security configuration

        authCheckerServiceAndUserFilter = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);

        authCheckerServiceAndUserFilter.setAuthenticationManager(authenticationManager);

        //print all headers
        LOG.info("::Authorization header done in filter");

    }

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
                .antMatchers(anonymousPaths.toArray(new String[0]));
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/actuator/**")
                .permitAll()
                .and()
                .sessionManagement()
                .sessionCreationPolicy(STATELESS)
                .and()
                .csrf()
                .disable()
                .formLogin()
                .disable()
                .logout()
                .disable()
                .authorizeRequests()
                .anyRequest()
                .authenticated()
                .and()
                .addFilter(authCheckerServiceAndUserFilter);
    }
}
