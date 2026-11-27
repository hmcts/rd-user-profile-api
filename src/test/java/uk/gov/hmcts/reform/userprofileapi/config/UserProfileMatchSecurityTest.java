package uk.gov.hmcts.reform.userprofileapi.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.validators.AuthTokenValidator;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileMatchController;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileMatchResponse;
import uk.gov.hmcts.reform.userprofileapi.oidc.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileMatcher;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserProfileMatchController.class)
@org.springframework.test.context.ContextConfiguration(
    classes = UserProfileMatchSecurityTest.ServiceConfiguration.class)
@Import({UserProfileMatchController.class, SecurityConfiguration.class, RestAuthenticationEntryPoint.class,
    SecurityEndpointFilter.class, UserProfileMatchSecurityTest.ServiceConfiguration.class})
class UserProfileMatchSecurityTest {
    @Autowired
    private MockMvc mvc;
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private JwtGrantedAuthoritiesConverter authoritiesConverter;
    @MockitoBean
    private AuthTokenValidator tokenValidator;
    @MockitoBean
    private UserProfileMatcher matcher;
    @MockitoBean
    private uk.gov.hmcts.reform.userprofileapi.service.FeatureToggleService featureToggleService;

    @BeforeEach
    void setUp() {
        var jwt = Jwt.withTokenValue("valid-user").header("alg", "RS256").subject("admin").build();
        when(jwtDecoder.decode("valid-user")).thenReturn(jwt);
        when(tokenValidator.getServiceName("Bearer valid-prd")).thenReturn("rd_professional_api");
        when(tokenValidator.getServiceName("Bearer other-service")).thenReturn("rd_user_profile_api");
        when(authoritiesConverter.convert(any())).thenReturn(List.of(new SimpleGrantedAuthority("prd-admin")));
        when(matcher.match(any(), any())).thenReturn(new UserProfileMatchResponse(List.of(), List.of()));
    }

    @Test
    void acceptsOnlyPrdServiceAndPrdAdminBearer() throws Exception {
        mvc.perform(post("/v1/userprofile/users/match").header("ServiceAuthorization", "Bearer valid-prd")
                        .header("Authorization", "Bearer valid-user").contentType(APPLICATION_JSON)
                        .content("{\"userIds\":[],\"fullNameContains\":\"\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAnotherGloballyAllowedService() throws Exception {
        mvc.perform(post("/v1/userprofile/users/match").header("ServiceAuthorization", "Bearer other-service")
                        .header("Authorization", "Bearer valid-user").contentType(APPLICATION_JSON)
                        .content("{\"userIds\":[],\"fullNameContains\":\"\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(matcher);
    }

    @Test
    void rejectsMissingServiceAndMissingBearerAndWrongRole() throws Exception {
        mvc.perform(post("/v1/userprofile/users/match").header("Authorization", "Bearer valid-user"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/v1/userprofile/users/match").header("ServiceAuthorization", "Bearer valid-prd"))
                .andExpect(status().isUnauthorized());
        when(authoritiesConverter.convert(any())).thenReturn(List.of(new SimpleGrantedAuthority("caseworker")));
        mvc.perform(post("/v1/userprofile/users/match").header("ServiceAuthorization", "Bearer valid-prd")
                        .header("Authorization", "Bearer valid-user"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(matcher);
    }

    @Test
    void rejectsInvalidBearerBeforeLookup() throws Exception {
        when(jwtDecoder.decode("invalid-user")).thenThrow(
                new org.springframework.security.oauth2.jwt.BadJwtException("Invalid token"));
        mvc.perform(post("/v1/userprofile/users/match").header("ServiceAuthorization", "Bearer valid-prd")
                        .header("Authorization", "Bearer invalid-user").contentType(APPLICATION_JSON)
                        .content("{\"userIds\":[],\"fullNameContains\":\"\"}"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(matcher);
    }

    @org.springframework.context.annotation.Configuration
    static class ServiceConfiguration {
        @Bean
        org.springframework.security.oauth2.client.registration.ClientRegistrationRepository registrations() {
            return new org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository(
                    org.springframework.security.oauth2.client.registration.ClientRegistration
                            .withRegistrationId("oidc").clientId("synthetic")
                            .authorizationGrantType(org.springframework.security.oauth2.core.AuthorizationGrantType
                                    .AUTHORIZATION_CODE)
                            .redirectUri("http://localhost/callback").authorizationUri("http://localhost/authorize")
                            .tokenUri("http://localhost/token").build());
        }

        @Bean
        ServiceAuthFilter serviceAuthFilter(AuthTokenValidator validator) {
            return new ServiceAuthFilter(validator, List.of("rd_professional_api", "rd_user_profile_api"));
        }
    }
}
