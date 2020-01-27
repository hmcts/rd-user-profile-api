package uk.gov.hmcts.reform.userprofileapi.service;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdentifierName;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileIdentifier;

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
            return () -> userProfileRepository.findByIdamId(id.getValue());
        }

        throw new IllegalStateException("Invalid User Profile identifier supplied.");
    }

    public Optional<List<UserProfile>> getProfilesByIds(UserProfileIdentifier id, boolean showDeleted) {

        List<String> userIds = id.getValues();

        if (showDeleted) {
            return userProfileRepository.findByIdamIdIn(userIds);
        } else {
            return userProfileRepository.findByIdamIdInAndStatusNot(userIds, IdamStatus.DELETED);
        }
    }

}
