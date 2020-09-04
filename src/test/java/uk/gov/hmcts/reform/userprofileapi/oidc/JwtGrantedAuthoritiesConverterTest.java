package uk.gov.hmcts.reform.userprofileapi.oidc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.constants.TestConstants;
import uk.gov.hmcts.reform.userprofileapi.repository.IdamRepository;

@ExtendWith(MockitoExtension.class)
public class JwtGrantedAuthoritiesConverterTest {

    @Mock
    IdamRepository idamRepositoryMock;
    @Mock
    UserInfo userInfoMock;
    @Mock
    Jwt jwtMock;
    @InjectMocks
    JwtGrantedAuthoritiesConverter converter;

    @Test
    public void test_shouldReturnEmptyAuthorities() {
        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    public void test_shouldReturnEmptyAuthoritiesWhenClaimNotAvailable() {
        when(jwtMock.containsClaim(anyString())).thenReturn(false);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    public void test_shouldReturnEmptyAuthoritiesWhenClaimValueNotEquals() {
        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("Test");

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(idamRepositoryMock, times(0)).getUserInfo(anyString());
    }

    @Test
    public void test_shouldReturnEmptyAuthoritiesWhenIdamReturnsNoUsers() {
        List<String> roles = new ArrayList<>();

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(0, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());

    }

    @Test
    public void test_shouldReturnAuthoritiesWhenIdamReturnsUserRoles() {
        List<String> roles = new ArrayList<>();
        roles.add(TestConstants.PUI_CASE_MANAGER);
        roles.add(TestConstants.PUI_FINANCE_MANAGER);

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        Collection<GrantedAuthority> authorities = converter.convert(jwtMock);

        assertNotNull(authorities);
        assertEquals(2, authorities.size());
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
    }

    @Test
    public void test_shouldReturnUserInfo() {
        List<String> roles = new ArrayList<>();
        roles.add(TestConstants.PUI_CASE_MANAGER);
        roles.add(TestConstants.PUI_FINANCE_MANAGER);

        when(jwtMock.containsClaim(anyString())).thenReturn(true);
        when(jwtMock.getClaim(anyString())).thenReturn("access_token");
        when(jwtMock.getTokenValue()).thenReturn("access_token");
        when(userInfoMock.getRoles()).thenReturn(roles);
        when(idamRepositoryMock.getUserInfo(anyString())).thenReturn(userInfoMock);

        converter.convert(jwtMock);
        UserInfo userInfo = converter.getUserInfo();

        assertThat(userInfo).isNotNull();
        verify(jwtMock, times(1)).containsClaim(anyString());
        verify(jwtMock, times(1)).getClaim(anyString());
        verify(jwtMock, times(1)).getTokenValue();
        verify(userInfoMock, times(1)).getRoles();
        verify(idamRepositoryMock, times(1)).getUserInfo(anyString());
    }
}
