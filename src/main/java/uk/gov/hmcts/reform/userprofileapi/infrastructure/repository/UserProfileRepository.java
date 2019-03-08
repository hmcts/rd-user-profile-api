package uk.gov.hmcts.reform.userprofileapi.infrastructure.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface UserProfileRepository extends CrudRepository<UserProfile, UUID> {

    Optional<UserProfile> findByEmail(String email);

    Optional<UserProfile> findByIdamId(String idamId);
}
