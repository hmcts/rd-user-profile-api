package uk.gov.hmcts.reform.userprofileapi.domain.service;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileIdentifier;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@Service
public class UserProfileQuerySupplier {

    private UserProfileRepository userProfileRepository;

    public UserProfileQuerySupplier(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public Supplier<Optional<UserProfile>> getIdQuery(UserProfileIdentifier id) {

        if (id.getName() == IdentifierName.IDAMID) {
            return () -> userProfileRepository.findByIdamId(id.getValue());
        } else if (id.getName() == IdentifierName.EMAIL) {
            return () -> userProfileRepository.findByEmail(id.getValue());
        } else if (id.getName() == IdentifierName.UUID) {
            return () -> userProfileRepository.findById(UUID.fromString(id.getValue()));
        }

        throw new IllegalStateException("Invalid User Profile identifier received");
    }


}
