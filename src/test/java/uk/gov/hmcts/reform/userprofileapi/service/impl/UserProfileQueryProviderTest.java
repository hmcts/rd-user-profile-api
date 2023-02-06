package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfileIdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;

import java.util.*;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.ResponseEntity.status;

@ExtendWith(MockitoExtension.class)
class UserProfileQueryProviderTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    @InjectMocks
    private UserProfileQueryProvider userProfileQueryProvider;

    private final IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(ACCEPTED).build());
    private final UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private final UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo
            .getIdamRegistrationResponse());

    @BeforeEach
    public void setUp() {
        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setIdamId("1234");
        userProfile.setId((long) 1234);

        openMocks(this);
    }

    @Test
    void test_getRetrieveByIdQuery_with_userIdentifier_email() {
        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByEmail(any(String.class))).thenReturn(Optional.of(userProfile));

        UserProfileIdentifier userProfileIdentifierWithOneValue = new UserProfileIdentifier(IdentifierName.EMAIL,
                userProfile.getEmail());

        Supplier<Optional<UserProfile>> userProfiles = userProfileQueryProvider
                .getRetrieveByIdQuery(userProfileIdentifierWithOneValue);

        assertThat(userProfiles).isNotNull();

        Optional<UserProfile> userProfile = userProfiles.get();

        assertTrue(userProfile.isPresent());
        assertThat(userProfile.get().getEmail()).isEqualTo(this.userProfile.getEmail());

        verify(userProfileRepositoryMock, Mockito.times(1)).findByEmail(any(String.class));
    }

    @Test
    void test_getRetrieveByIdQuery_with_userIdentifier_userId() {
        userProfileRepositoryMock.save(userProfile);

        UUID userId = UUID.randomUUID();

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.of(userProfile));

        UserProfileIdentifier userProfileIdentifierWithOneValue
                = new UserProfileIdentifier(IdentifierName.UUID, userId.toString());

        Supplier<Optional<UserProfile>> userProfiles
                = userProfileQueryProvider.getRetrieveByIdQuery(userProfileIdentifierWithOneValue);

        assertThat(userProfiles).isNotNull();

        Optional<UserProfile> userProfile = userProfiles.get();
        assertTrue(userProfile.isPresent());
        assertThat(userProfile.get().getEmail()).isEqualTo(userProfile.get().getEmail());

        verify(userProfileRepositoryMock, Mockito.times(1)).findByIdamId(any(String.class));
    }


    @Test
    void test_getRetrieveByIdQuery_ThrowsIllegalStateException() {
        UserProfileIdentifier userProfileIdentifierWithOneValue =
                new UserProfileIdentifier(null, userProfile.getEmail());
        assertThrows(IllegalStateException.class, () -> userProfileQueryProvider
                .getRetrieveByIdQuery(userProfileIdentifierWithOneValue));
    }

    @Test
    void test_getProfilesByIds() {
        List<UserProfile> userProfiles = Collections.singletonList(userProfile);

        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByIdamIdIn(anyList())).thenReturn(Optional.of(userProfiles));

        UserProfileIdentifier userProfileIdentifierWithMultipleValue
                = new UserProfileIdentifier(IdentifierName.UUID_LIST,
                Collections.singletonList(userProfile.getIdamId()));

        Optional<List<UserProfile>> result = userProfileQueryProvider
                .getProfilesByIds(userProfileIdentifierWithMultipleValue, Boolean.TRUE);

        assertTrue(result.isPresent());

        assertThat(result.get().get(0).getEmail()).isEqualTo(userProfile.getEmail());
        verify(userProfileRepositoryMock, Mockito.times(1)).findByIdamIdIn(anyList());
    }

    @Test
    void test_getProfilesByIds_with_show_deleted_false() {
        List<UserProfile> userProfiles = Collections.singletonList(userProfile);

        userProfileRepositoryMock.save(userProfile);

        UserProfileIdentifier userProfileIdentifierWithMultipleValue
                = new UserProfileIdentifier(IdentifierName.UUID_LIST,
                Collections.singletonList(userProfile.getIdamId()));


        when(userProfileRepositoryMock.findByIdamIdInAndStatusNot(any(), any(IdamStatus.class)))
                .thenReturn(Optional.of(userProfiles));

        Optional<List<UserProfile>> result
                = userProfileQueryProvider.getProfilesByIds(userProfileIdentifierWithMultipleValue, Boolean.FALSE);

        assertTrue(result.isPresent());
        assertThat(result.get().get(0).getEmail()).isEqualTo(userProfile.getEmail());
        verify(userProfileRepositoryMock, Mockito.times(1)).findByIdamIdInAndStatusNot(any(),
                any(IdamStatus.class));
    }

    @Test
    void test_findByUserCategory() {
        UserProfileIdamStatus status = buildIdamStatus();
        when(userProfileRepositoryMock.findByUserCategory(UserCategory.CASEWORKER)).thenReturn(List.of(status));
        List<UserProfileIdamStatus> userProfiles = userProfileQueryProvider
                .getProfilesByUserCategory("CASEWORKER");
        verify(userProfileRepositoryMock, Mockito.times(1)).findByUserCategory(UserCategory.CASEWORKER);
        assertThat(userProfiles).isNotNull();
        assertThat(Objects.requireNonNull(userProfiles)
                .get(0).getEmail())
                .isEqualTo("test@email.com");
        assertThat(Objects.requireNonNull(userProfiles)
                .get(0).getStatus())
                .isEqualTo("pending");
    }


    @Test
    void test_findByUserCategory_ThrowsException() {
        assertThatThrownBy(() ->  userProfileQueryProvider.getProfilesByUserCategory("caseworker"))
                .hasMessage("Invalid userCategory supplied.");
    }


    @NotNull
    private static UserProfileIdamStatus buildIdamStatus() {
        UserProfileIdamStatus status = new UserProfileIdamStatus() {
            @Override
            public String getEmail() {
                return "test@email.com";
            }

            @Override
            public String getStatus() {
                return "pending";
            }
        };
        return status;
    }


}
