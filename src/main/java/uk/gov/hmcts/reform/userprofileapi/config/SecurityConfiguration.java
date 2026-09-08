package uk.gov.hmcts.reform.userprofileapi.config;

import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.userprofileapi.config.security.IdamSecurityProperties;
import uk.gov.hmcts.reform.userprofileapi.oidc.JwtGrantedAuthoritiesConverter;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@ConfigurationProperties(prefix = "security")
@EnableWebSecurity
@Slf4j
@SuppressWarnings("unchecked")
public class SecurityConfiguration {

    @Value("${spring.security.oauth2.client.provider.oidc.issuer-uri}")
    private String issuerUri;

    @Order(1)
    private final ServiceAuthFilter serviceAuthFilter;
    @Order(2)
    private final SecurityEndpointFilter securityEndpointFilter;
    List<String> anonymousPaths;

    private JwtAuthenticationConverter jwtAuthenticationConverter;

    private RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    public List<String> getAnonymousPaths() {
        return anonymousPaths;
    }

    public void setAnonymousPaths(List<String> anonymousPaths) {
        this.anonymousPaths = anonymousPaths;
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring().requestMatchers(anonymousPaths.toArray(String[]::new));
    }

    @Inject
    public SecurityConfiguration(final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
                                 final ServiceAuthFilter serviceAuthFilter,
                                 RestAuthenticationEntryPoint restAuthenticationEntryPoint,
                                 SecurityEndpointFilter securityEndpointFilter) {

        this.serviceAuthFilter = serviceAuthFilter;
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
        jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
        this.securityEndpointFilter = securityEndpointFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.addFilterBefore(serviceAuthFilter, BearerTokenAuthenticationFilter.class)
                .addFilterAfter(securityEndpointFilter, OAuth2AuthorizationRequestRedirectFilter.class)
                .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.requestMatchers("/error").permitAll().anyRequest().authenticated())
                .oauth2ResourceServer(auth ->
                        auth.authenticationEntryPoint(restAuthenticationEntryPoint)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
                .oauth2Client(Customizer.withDefaults());
        return http.build();
    }

    @Bean
    JwtDecoder jwtDecoder(IdamSecurityProperties securityProperties) {

        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);
        jwtDecoder.setJwtValidator(getIssuerValidator(securityProperties));
        return jwtDecoder;
    }

    private OAuth2TokenValidator<Jwt> getIssuerValidator(IdamSecurityProperties securityProperties) {
        OAuth2TokenValidator<Jwt> withTimestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> validator;
        if (Boolean.parseBoolean(System.getProperty("idam.security.issuerValidation"))) {
            log.debug("Validating issuers");
            validator = new DelegatingOAuth2TokenValidator<>(withTimestamp,
                    allowedIssuersValidator(securityProperties.getAllowedIssuers())
            );
        } else {
            log.debug("Validating timestamp");
            validator = new DelegatingOAuth2TokenValidator<>(withTimestamp);
        }
        return validator;
    }

    protected OAuth2TokenValidator<Jwt> allowedIssuersValidator(List<String> allowedIssuers) {
        Set<String> allowedIssuerSet = Set.copyOf(allowedIssuers);
        return new JwtClaimValidator<>("iss", iss -> Objects.nonNull(iss) && allowedIssuerSet.contains(iss));
    }
}


