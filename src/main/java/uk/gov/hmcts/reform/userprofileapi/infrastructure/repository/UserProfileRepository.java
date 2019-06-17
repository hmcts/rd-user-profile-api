package uk.gov.hmcts.reform.userprofileapi.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, UUID> {

    @Transactional(readOnly = true, timeout = 10)
    Optional<UserProfile> findByEmail(String email);

    @Transactional(readOnly = true, timeout = 10)
    Optional<UserProfile> findById(String id);
}
