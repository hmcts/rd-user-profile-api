package uk.gov.hmcts.reform.userprofileapi.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.hmcts.reform.userprofileapi.clients.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.controllers.ResourceNotFoundException;
import uk.gov.hmcts.reform.userprofileapi.data.UserProfileTestDataBuilder;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileRetrieverImpl;

@RunWith(MockitoJUnitRunner.class)
public class UserProfileRetrieverImplTest {

    @InjectMocks
    private UserProfileRetrieverImpl userProfileRetrieverImpl;

    @Mock
    private UserProfileQueryProvider querySupplier;

    @Mock
    private Supplier<Optional<UserProfile>> supplier;

    @Test
    public void should_run_query_and_respond_with_user_profile() {

        UserProfile userProfile = UserProfileTestDataBuilder.buildUserProfile();

        Stream.of(IdentifierName.values())
            .forEach(identifierName -> {
                    UserProfileIdentifier identifier = new UserProfileIdentifier(
                        identifierName,
                        String.valueOf(new Random().nextInt()));

                    when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
                    when(supplier.get()).thenReturn(Optional.of(userProfile));

                    UserProfile entity = userProfileRetrieverImpl.retrieve(identifier, false);
                    assertThat(entity).isEqualTo(userProfile);
                }
            );
    }

    @Test
    public void should_throw_exception_when_query_returns_empty_result() {

        UserProfileIdentifier identifier =
            new UserProfileIdentifier(
                IdentifierName.UUID,
                UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userProfileRetrieverImpl.retrieve(identifier, false))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Could not find resource from database with given identifier: " + identifier.getValue());

    }

    @Test
    public void should_throw_exception_when_query_provider_throws_exception() {

        UserProfileIdentifier identifier =
            new UserProfileIdentifier(
                IdentifierName.UUID,
                UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenThrow(IllegalStateException.class);

        assertThatThrownBy(() -> userProfileRetrieverImpl.retrieve(identifier, false))
            .isInstanceOf(IllegalStateException.class);

    }

    @Test
    public void should_throw_exception_when_query_throws_exception() {

        UserProfileIdentifier identifier =
            new UserProfileIdentifier(
                IdentifierName.UUID,
                UUID.randomUUID().toString());

        when(querySupplier.getRetrieveByIdQuery(identifier)).thenReturn(supplier);
        when(supplier.get()).thenThrow(ResourceNotFoundException.class);

        assertThatThrownBy(() -> userProfileRetrieverImpl.retrieve(identifier, false))
            .isInstanceOf(ResourceNotFoundException.class);

    }
}
