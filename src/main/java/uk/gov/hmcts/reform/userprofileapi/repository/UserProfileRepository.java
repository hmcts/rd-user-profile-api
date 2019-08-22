package uk.gov.hmcts.reform.userprofileapi.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {

    @Transactional(readOnly = true)
    Optional<UserProfile> findByEmail(String email);

    @Transactional(readOnly = true)
    Optional<UserProfile> findByIdamId(UUID id);

    @Transactional(readOnly = true)
    Optional<List<UserProfile>> findByIdamIdInAndStatusNot(List<UUID> userIds, IdamStatus idamStatus);

    @Transactional(readOnly = true)
    Optional<List<UserProfile>> findByIdamIdIn(List<UUID> userIds);
}
