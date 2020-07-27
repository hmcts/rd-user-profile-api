package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.ResponseEntity.status;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationHelperService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;


@RunWith(MockitoJUnitRunner.class)
public class ValidationServiceImplTest {

    private final String userId = UUID.randomUUID().toString();

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @Mock
    private ValidationHelperService validationHelperServiceMock;

    private UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(ACCEPTED).build());
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo
            .getIdamRegistrationResponse());

    private UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData("email@net.com",
            "firstName", "lastName", "ACTIVE", new HashSet<RoleName>(),
            new HashSet<RoleName>());


    @InjectMocks
    private ValidationService sut = new ValidationServiceImpl();

    @Before
    public void setUp() {
        when(userProfileRepositoryMock.findByIdamId(eq(userId))).thenReturn(Optional.of(userProfile));
    }

    @Test
    public void test_ValidateUpdateWithoutId() {
        userProfile.setStatus(IdamStatus.SUSPENDED);

        when(validationHelperServiceMock.validateUserId(eq(userId))).thenReturn(true);
        when(validationHelperServiceMock.validateUpdateUserProfileRequestValid(updateUserProfileData, userId,
                ResponseSource.API)).thenReturn(true);

        UserProfile actual = sut.validateUpdate(updateUserProfileData, userId, ResponseSource.API);

        assertThat(actual.getStatus()).isEqualTo(IdamStatus.SUSPENDED);

        verify(userProfileRepositoryMock, times(1)).findByIdamId(any(String.class));
        verify(validationHelperServiceMock, times(1)).validateUserIsPresent(any());
    }

    @Test
    public void test_IsValidForUserDetailUpdateHappyPath() {
        when(validationHelperServiceMock.validateUserStatusBeforeUpdate(updateUserProfileData, userProfile,
                ResponseSource.API)).thenReturn(true);
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileData, userProfile, ResponseSource.API)).isTrue();
    }

    @Test
    public void test_IsValidForUserDetailUpdateSadPath() {
        assertThat(sut.isValidForUserDetailUpdate(updateUserProfileData, userProfile, ResponseSource.API)).isFalse();
        verify(validationHelperServiceMock, times(1))
                .validateUserStatusBeforeUpdate(any(UpdateUserProfileData.class), any(UserProfile.class),
                        any(ResponseSource.class));
    }

    @Test
    public void test_IsExuiUpdateRequest() {
        assertThat(sut.isExuiUpdateRequest(ResponseSource.EXUI.name())).isTrue();
        assertThat(sut.isExuiUpdateRequest("INVALID")).isFalse();
    }

}