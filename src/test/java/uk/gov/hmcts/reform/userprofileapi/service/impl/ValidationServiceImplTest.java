package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
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

    private final UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(ACCEPTED).build());
    private final UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo
            .getIdamRegistrationResponse());

    private final UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com",
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
}
