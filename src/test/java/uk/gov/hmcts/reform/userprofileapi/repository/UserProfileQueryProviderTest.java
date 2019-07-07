package uk.gov.hmcts.reform.userprofileapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.clients.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;


@Ignore
@RunWith(MockitoJUnitRunner.class)
public class UserProfileQueryProviderTest {

    @InjectMocks
    private UserProfileQueryProvider queryProvider;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Test
    public void should_query_by_uuid_successfully() {

        UserProfile userProfile = mock(UserProfile.class);
        Long id = 1L;
        UUID uuid = UUID.randomUUID();
        when(userProfileRepository.findById(id)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.UUID, uuid.toString()));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        verify(userProfileRepository).findById(id);
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
        UUID id = UUID.randomUUID();//String.valueOf(new Random().nextInt());

        when(userProfileRepository.findByIdamId(id)).thenReturn(Optional.of(userProfile));

        Supplier<Optional<UserProfile>> querySupplier =
            queryProvider.getRetrieveByIdQuery(new UserProfileIdentifier(IdentifierName.UUID, id.toString()));

        Optional<UserProfile> optionalProfile = querySupplier.get();

        assertThat(optionalProfile.isPresent()).isTrue();
        assertThat(optionalProfile.get()).isEqualTo(userProfile);

        /*verify(userProfileRepository).findById(id);
        verifyNoMoreInteractions(userProfileRepository);*/

    }

    @Test
    public void should_thow_exception_if_no_query_found() {

        UserProfileIdentifier identifier = mock(UserProfileIdentifier.class);

        when(identifier.getName()).thenReturn(null);

        assertThatThrownBy(() -> queryProvider.getRetrieveByIdQuery(identifier))
            .isInstanceOf(IllegalStateException.class);

    }

}
