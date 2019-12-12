package uk.gov.hmcts.reform.userprofileapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.*;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileQueryProviderTest {

    @InjectMocks
    private UserProfileQueryProvider queryProvider;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    public void should_query_by_uuid_successfully() {

        UserProfile userProfile = Mockito.mock(UserProfile.class);
        Long id = 1L;
        UUID uuid = UUID.randomUUID();
        Mockito.when(userProfileRepository.findByIdamId("1")).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.UUID, "1"));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        Mockito.verify(userProfileRepository).findByIdamId("1");
        Mockito.verifyNoMoreInteractions(userProfileRepository);

    }


    @Test
    public void should_query_by_email_successfully() {

        UserProfile userProfile = Mockito.mock(UserProfile.class);
        String id = String.valueOf(new Random().nextInt());
        Mockito.when(userProfileRepository.findByEmail(id)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.EMAIL, id));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        Mockito.verify(userProfileRepository).findByEmail(id);
        Mockito.verifyNoMoreInteractions(userProfileRepository);

    }

    @Test
    public void should_query_by_idamId_successfully() {

        UserProfile userProfile = Mockito.mock(UserProfile.class);
        String id = UUID.randomUUID().toString();

        Mockito.when(userProfileRepository.findByIdamId(id)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.UUID, id.toString()));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

    }

    @Test
    public void should_thow_exception_if_no_query_found() {

        UserProfileIdentifier identifier = Mockito.mock(UserProfileIdentifier.class);

        Mockito.when(identifier.getName()).thenReturn(null);

        assertThatThrownBy(() -> queryProvider.getRetrieveByIdQuery(identifier))
            .isInstanceOf(IllegalStateException.class);

    }

    @Test
    public void should_query_by_multiple_idamId_with_showDeleted_true_successfully() {

        UserProfile userProfile1 = Mockito.mock(UserProfile.class);
        UserProfile userProfile2 = Mockito.mock(UserProfile.class);
        List<UserProfile> profiles = new ArrayList<UserProfile>();
        profiles.add(userProfile1);
        profiles.add(userProfile2);

        List<String> stringUserIds = new ArrayList<>();
        stringUserIds.add(UUID.randomUUID().toString());
        stringUserIds.add(UUID.randomUUID().toString());

        Mockito.when(userProfileRepository.findByIdamIdIn(Mockito.any())).thenReturn(Optional.of(profiles));

        Optional<List<UserProfile>> querySupplier =
                queryProvider.getProfilesByIds(new UserProfileIdentifier(IdentifierName.UUID_LIST, stringUserIds), true);

        List<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.size()).isEqualTo(2);
    }

    @Test
    public void should_query_by_multiple_idamId_with_showDeleted_false_successfully() {

        UserProfile userProfile1 = Mockito.mock(UserProfile.class);
        UserProfile userProfile2 = Mockito.mock(UserProfile.class);
        List<UserProfile> profiles = new ArrayList<UserProfile>();
        profiles.add(userProfile1);
        profiles.add(userProfile2);

        String id1 = UUID.randomUUID().toString();
        String id2 = UUID.randomUUID().toString();

        List<String> stringUserIds = new ArrayList<>();
        stringUserIds.add(id1);
        stringUserIds.add(id2);

        List<String> ids = new ArrayList<>();
        ids.add(id1);
        ids.add(id2);

        Mockito.when(userProfileRepository.findByIdamIdInAndStatusNot(ids, IdamStatus.DELETED)).thenReturn(Optional.of(profiles));

        Optional<List<UserProfile>> querySupplier =
                queryProvider.getProfilesByIds(new UserProfileIdentifier(IdentifierName.UUID_LIST, stringUserIds), false);

        List<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.size()).isEqualTo(2);
    }

}
