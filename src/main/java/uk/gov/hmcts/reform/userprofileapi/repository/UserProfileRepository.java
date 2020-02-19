package uk.gov.hmcts.reform.userprofileapi.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

@Repository
public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {

    @Transactional(readOnly = true)
    Optional<UserProfile> findByEmail(String email);

    @Transactional(readOnly = true)
    Optional<UserProfile> findByIdamId(String id);

    @Transactional(readOnly = true)
    Optional<List<UserProfile>> findByIdamIdInAndStatusNot(List<String> userIds, IdamStatus idamStatus);

    @Transactional(readOnly = true)
    Optional<List<UserProfile>> findByIdamIdIn(List<String> userIds);
}
