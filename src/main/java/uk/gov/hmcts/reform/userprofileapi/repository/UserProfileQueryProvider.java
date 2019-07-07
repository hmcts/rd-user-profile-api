package uk.gov.hmcts.reform.userprofileapi.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.client.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@Service
public class UserProfileQueryProvider {

    private UserProfileRepository userProfileRepository;

    public UserProfileQueryProvider(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public Supplier<Optional<UserProfile>> getRetrieveByIdQuery(UserProfileIdentifier id) {

        if (id.getName() == IdentifierName.EMAIL) {
            return () -> userProfileRepository.findByEmail(id.getValue());
        } else if (id.getName() == IdentifierName.UUID) {
            return () -> userProfileRepository.findByIdamId(UUID.fromString(id.getValue()));
        }

        throw new IllegalStateException("Invalid User Profile identifier supplied.");
    }

}
