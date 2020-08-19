package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.ResponseEntity.status;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileQueryProviderTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    private UserProfileQueryProvider userProfileQueryProvider;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(status(ACCEPTED).build());
    private UserProfileCreationData userProfileCreationData = CreateUserProfileTestDataBuilder
            .buildCreateUserProfileData();
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo
            .getIdamRegistrationResponse());

    @Before
    public void setUp() {
        userProfileQueryProvider = new UserProfileQueryProvider(userProfileRepositoryMock);

        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setIdamId("1234");
        userProfile.setId((long) 1234);
    }

    @Test
    public void test_getRetrieveByIdQuery_with_userIdentifier_email() {
        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByEmail(any(String.class))).thenReturn(Optional.of(userProfile));

        UserProfileIdentifier userProfileIdentifierWithOneValue = new UserProfileIdentifier(IdentifierName.EMAIL,
                userProfile.getEmail());

        Supplier<Optional<UserProfile>> userProfiles = userProfileQueryProvider
                .getRetrieveByIdQuery(userProfileIdentifierWithOneValue);

        assertThat(userProfiles).isNotNull();
        assertThat(userProfiles.get().get().getEmail()).isEqualTo(userProfile.getEmail());

        verify(userProfileRepositoryMock, Mockito.times(1)).findByEmail(any(String.class));
    }

    @Test
    public void test_getRetrieveByIdQuery_with_userIdentifier_userId() {
        userProfileRepositoryMock.save(userProfile);

        UUID userId = UUID.randomUUID();

        when(userProfileRepositoryMock.findByIdamId(any(String.class))).thenReturn(Optional.of(userProfile));

        UserProfileIdentifier userProfileIdentifierWithOneValue
                = new UserProfileIdentifier(IdentifierName.UUID, userId.toString());

        Supplier<Optional<UserProfile>> userProfiles
                = userProfileQueryProvider.getRetrieveByIdQuery(userProfileIdentifierWithOneValue);

        assertThat(userProfiles).isNotNull();
        assertThat(userProfiles.get().get().getEmail()).isEqualTo(userProfile.getEmail());

        verify(userProfileRepositoryMock, Mockito.times(1)).findByIdamId(any(String.class));
    }


    @Test(expected = IllegalStateException.class)
    public void test_getRetrieveByIdQuery_ThrowsIllegalStateException() {
        UserProfileIdentifier userProfileIdentifierWithOneValue = new UserProfileIdentifier(null,
                userProfile.getEmail());
        userProfileQueryProvider.getRetrieveByIdQuery(userProfileIdentifierWithOneValue);
    }

    @Test
    public void test_getProfilesByIds() {
        List<UserProfile> userProfiles = Collections.singletonList(userProfile);

        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByIdamIdIn(anyList())).thenReturn(Optional.of(userProfiles));

        UserProfileIdentifier userProfileIdentifierWithMultipleValue
                = new UserProfileIdentifier(IdentifierName.UUID_LIST,
                Collections.singletonList(userProfile.getIdamId()));

        Optional<List<UserProfile>> result = userProfileQueryProvider
                .getProfilesByIds(userProfileIdentifierWithMultipleValue, Boolean.TRUE);

        assertThat(result).isNotNull();
        assertThat(result.get().get(0).getEmail()).isEqualTo(userProfile.getEmail());
        verify(userProfileRepositoryMock, Mockito.times(1)).findByIdamIdIn(anyList());
    }

    @Test
    public void test_getProfilesByIds_with_show_deleted_false() {
        List<UserProfile> userProfiles = Collections.singletonList(userProfile);

        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByIdamIdInAndStatusNot(any(), any(IdamStatus.class)))
                .thenReturn(Optional.of(userProfiles));

        UserProfileIdentifier userProfileIdentifierWithMultipleValue
                = new UserProfileIdentifier(IdentifierName.UUID_LIST,
                Collections.singletonList(userProfile.getIdamId()));

        Optional<List<UserProfile>> result
                = userProfileQueryProvider.getProfilesByIds(userProfileIdentifierWithMultipleValue, Boolean.FALSE);

        assertThat(result).isNotNull();
        assertThat(result.get().get(0).getEmail()).isEqualTo(userProfile.getEmail());
        verify(userProfileRepositoryMock, Mockito.times(1)).findByIdamIdInAndStatusNot(any(),
                any(IdamStatus.class));
    }
}
