package uk.gov.hmcts.reform.userprofileapi.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileQueryProviderTest {

    @InjectMocks
    private UserProfileQueryProvider queryProvider;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    public void should_query_by_uuid_successfully() {

        UserProfile userProfile = mock(UserProfile.class);
        UUID uuid = UUID.randomUUID();
        when(userProfileRepository.findById(uuid)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.UUID, uuid.toString()));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        verify(userProfileRepository).findById(uuid);
        verifyNoMoreInteractions(userProfileRepository);

    }


    @Test
    public void should_query_by_email_successfully() {

        UserProfile userProfile = mock(UserProfile.class);
        String id = String.valueOf(new Random().nextInt());
        when(userProfileRepository.findByEmail(id)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.EMAIL, id));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        verify(userProfileRepository).findByEmail(id);
        verifyNoMoreInteractions(userProfileRepository);

    }

    @Test
    public void should_query_by_idamId_successfully() {

        UserProfile userProfile = mock(UserProfile.class);
        String id = String.valueOf(new Random().nextInt());

        when(userProfileRepository.findByIdamId(id)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.IDAMID, id));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        verify(userProfileRepository).findByIdamId(id);
        verifyNoMoreInteractions(userProfileRepository);

    }

}
