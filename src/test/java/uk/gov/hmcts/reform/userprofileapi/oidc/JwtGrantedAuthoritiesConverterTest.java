package uk.gov.hmcts.reform.userprofileapi.oidc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.constants.TestConstants;
import uk.gov.hmcts.reform.userprofileapi.repository.IdamRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtGrantedAuthoritiesConverterTest {

    JwtGrantedAuthoritiesConverter converter;
    IdamRepository idamRepositoryMock;
    UserInfo userInfoMock;
    Jwt jwtMock;

    @BeforeEach
    public void setUp() {
        idamRepositoryMock = mock(IdamRepository.class);
        userInfoMock = mock(UserInfo.class);
        jwtMock = mock(Jwt.class);
        converter = new JwtGrantedAuthoritiesConverter(idamRepositoryMock);
    }

    @Test
    void testShouldReturnEmptyAuthorities() {
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        Assertions.assertEquals(0, authorities.size());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnEmptyAuthoritiesWhenClaimNotAvailable() {
        when(jwtMock.hasClaim(anyString())).thenReturn(false);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertThat(authorities).isEmpty();
        verify(jwtMock, times(1)).hasClaim(anyString());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnEmptyAuthoritiesWhenClaimValueNotEquals() {
        when(jwtMock.hasClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("Test");

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertThat(authorities).isEmpty();
        verify(jwtMock, times(1)).hasClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    void test_shouldReturnEmptyAuthoritiesWhenIdamReturnsNoUsers() {
        List<String> roles = new ArrayList<>();

        when(jwtMock.hasClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertThat(authorities).isEmpty();
        verify(jwtMock, times(1)).hasClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());

    }

    @Test
    void test_shouldReturnAuthoritiesWhenIdamReturnsUserRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(TestConstants.PUI_CASE_MANAGER);
        roles.add(TestConstants.PUI_FINANCE_MANAGER);

        when(jwtMock.hasClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertThat(authorities).isNotEmpty().hasSize(2);
        verify(jwtMock, times(1)).hasClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
    }

    @Test
    void overlappingCallersKeepTheirOwnRoles() throws Exception {
        UserInfo admin = mock(UserInfo.class);
        UserInfo ordinary = mock(UserInfo.class);
        when(admin.getRoles()).thenReturn(List.of("prd-admin"));
        when(ordinary.getRoles()).thenReturn(List.of("caseworker"));
        var barrier = new java.util.concurrent.CyclicBarrier(2);
        when(idamRepositoryMock.getUserInfo(anyString())).thenAnswer(invocation -> {
            barrier.await(5, java.util.concurrent.TimeUnit.SECONDS);
            return "admin".equals(invocation.getArgument(0)) ? admin : ordinary;
        });
        try (var pool = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var tasks = new ArrayList<java.util.concurrent.Callable<Void>>();
            for (String token : List.of("admin", "ordinary")) {
                tasks.add(() -> {
                    var jwt = Jwt.withTokenValue(token).header("alg", "RS256")
                            .claim("tokenName", "access_token").build();
                    var snapshots = new ArrayList<Collection<GrantedAuthority>>();
                    for (int attempt = 0; attempt < 100; attempt++) {
                        snapshots.add(converter.convert(jwt));
                    }
                    assertThat(snapshots).allSatisfy(authorities -> assertThat(authorities)
                            .extracting(GrantedAuthority::getAuthority)
                            .containsExactly("admin".equals(token) ? "prd-admin" : "caseworker"));
                    return null;
                });
            }
            for (var future : pool.invokeAll(tasks)) {
                future.get(10, java.util.concurrent.TimeUnit.SECONDS);
            }
        }
    }
}
