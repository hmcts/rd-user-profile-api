package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.exception.ForbiddenException;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.IdamRepository;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.ResponseEntity.status;


@ExtendWith(MockitoExtension.class)
class ValidationServiceImplTest {

    private final String userId = UUID.randomUUID().toString();

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @Mock
    private ValidationHelperService validationHelperServiceMock;

    @Mock
    private IdamRepository idamRepository;

    private final UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(ACCEPTED).build());
    private final UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo
            .getIdamRegistrationResponse());

    private final UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("test@test.com",
            "firstName", "lastName", "ACTIVE", new HashSet<>(),
            new HashSet<>());


    @InjectMocks
    private final ValidationService sut = new ValidationServiceImpl();

    @Test
    void test_ValidateUpdateWithoutId() {
        userProfile.setStatus(IdamStatus.SUSPENDED);

        when(userProfileRepositoryMock.findByIdamId(userId)).thenReturn(Optional.of(userProfile));
        when(validationHelperServiceMock.validateUserId(userId)).thenReturn(true);
        when(validationHelperServiceMock.validateUpdateUserProfileRequestValid(updateUserProfileData, userId,
                ResponseSource.API)).thenReturn(true);

        UserProfile actual = sut.validateUpdate(updateUserProfileData, userId, ResponseSource.API);

        assertThat(actual.getStatus()).isEqualTo(IdamStatus.SUSPENDED);

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(validationHelperServiceMock, times(1)).validateUserIsPresent(any());
    }

    @Test
    void test_IsValidForUserDetailUpdateHappyPath() {
        when(validationHelperServiceMock.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile,
                ResponseSource.API)).thenReturn(true);
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileData, userProfile, ResponseSource.API)).isTrue();
    }

    @Test
    void test_IsValidForUserDetailUpdateSadPath() {
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileData, userProfile, ResponseSource.API)).isFalse();
        verify(validationHelperServiceMock, times(1))
                .validateUserStatusBeforeUpdate(any(UpdateUserProfileData.class), any(UserProfile.class),
                        any(ResponseSource.class));
    }

    @Test
    void test_IsExuiUpdateRequest() {
        assertThat(sut.isExuiUpdateRequest(ResponseSource.EXUI.name())).isTrue();
        assertThat(sut.isExuiUpdateRequest("INVALID")).isFalse();
    }

    @Test
    void test_ValidateUserEmailMatchesAuthenticatedPrincipal() {
        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", "test-user", "email", "test@test.com"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        sut.validateUserEmailMatchesAuthenticatedPrincipal("test@test.com");

        verify(idamRepository, times(0)).getUserInfo(any(String.class));
        SecurityContextHolder.clearContext();
    }

    @Test
    void test_ValidateUserEmailMatchesAuthenticatedPrincipal_shouldThrowForMismatch() {
        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", "test-user", "email", "test@test.com"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThatThrownBy(() -> sut.validateUserEmailMatchesAuthenticatedPrincipal("other@test.com"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("UserEmail header does not match authenticated user");

        SecurityContextHolder.clearContext();
    }

    @Test
    void test_ValidateUserEmailMatchesAuthenticatedPrincipal_shouldFallbackToIdamUserInfo() {
        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", "test-user"));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        UserInfo userInfo = UserInfo.builder().name("test@test.com").build();
        when(idamRepository.getUserInfo("token-value")).thenReturn(userInfo);

        sut.validateUserEmailMatchesAuthenticatedPrincipal("test@test.com");

        verify(idamRepository, times(1)).getUserInfo("token-value");
        SecurityContextHolder.clearContext();
    }

    @Test
    void test_validateUserIdMatchesAuthenticatedPrincipal() {
        String requestedUserId = UUID.randomUUID().toString();
        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", requestedUserId));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        sut.validateUserIdMatchesAuthenticatedPrincipal(requestedUserId);

        SecurityContextHolder.clearContext();
    }

    @Test
    void test_validateUserIdMatchesAuthenticatedPrincipal_shouldThrowForMismatch() {
        Jwt jwt = new Jwt("token-value", Instant.now(), Instant.now().plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", UUID.randomUUID().toString()));
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        assertThatThrownBy(() -> sut.validateUserIdMatchesAuthenticatedPrincipal(UUID.randomUUID().toString()))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("userId does not match authenticated user");

        SecurityContextHolder.clearContext();
    }
}
