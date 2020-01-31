package uk.gov.hmcts.reform.userprofileapi.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileDataTestBuilder;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileQueryProviderTest {

    @Mock
    private UserProfileRepository userProfileRepositoryMock;

    private UserProfileQueryProvider userProfileQueryProvider;

    private IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
    private UserProfileCreationData userProfileCreationData = CreateUserProfileDataTestBuilder.buildCreateUserProfileData();
    private UserProfile userProfile = new UserProfile(userProfileCreationData, idamRegistrationInfo.getIdamRegistrationResponse());

    @Before
    public void setUp() {
        userProfileQueryProvider = new UserProfileQueryProvider(userProfileRepositoryMock);

        userProfile.setStatus(IdamStatus.ACTIVE);
        userProfile.setIdamId("1234");
        userProfile.setId((long) 1234);
    }

    @Test
    public void getRetrieveByIdQueryTest() {
        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByEmail(any(String.class))).thenReturn(Optional.of(userProfile));

        UserProfileIdentifier userProfileIdentifierWithOneValue = new UserProfileIdentifier(IdentifierName.EMAIL, userProfile.getEmail());

        Supplier<Optional<UserProfile>> userProfiles = userProfileQueryProvider.getRetrieveByIdQuery(userProfileIdentifierWithOneValue);

        assertThat(userProfiles).isNotNull();
        assertThat(userProfiles.get().get().getEmail()).isEqualTo(userProfile.getEmail());
    }


    @Test(expected = IllegalStateException.class)
    public void getRetrieveByIdQueryTest_ThrowsIllegalStateException() {
        UserProfileIdentifier userProfileIdentifierWithOneValue = new UserProfileIdentifier(null, userProfile.getEmail());
        userProfileQueryProvider.getRetrieveByIdQuery(userProfileIdentifierWithOneValue);
    }

    @Test
    public void getProfilesByIdsTest() {
        List<UserProfile> userProfiles = Collections.singletonList(userProfile);

        userProfileRepositoryMock.save(userProfile);

        when(userProfileRepositoryMock.findByIdamIdIn(anyList())).thenReturn(Optional.of(userProfiles));

        UserProfileIdentifier userProfileIdentifierWithMultipleValue = new UserProfileIdentifier(IdentifierName.UUID_LIST, Collections.singletonList(userProfile.getIdamId()));

        Optional<List<UserProfile>> result = userProfileQueryProvider.getProfilesByIds(userProfileIdentifierWithMultipleValue, Boolean.TRUE);

        assertThat(result).isNotNull();
        assertThat(result.get().get(0).getEmail()).isEqualTo(userProfile.getEmail());
    }

}
