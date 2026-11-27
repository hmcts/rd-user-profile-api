package uk.gov.hmcts.reform.userprofileapi.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfileIdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {

    @Transactional(readOnly = true)
    @Query(value = """
            SELECT idam_id AS "userIdentifier", first_name AS "firstName", last_name AS "lastName",
                   email_address AS email,
                   lower(first_name || ' ' || last_name) LIKE lower(:pattern) ESCAPE '!' AS "nameMatches"
            FROM {h-schema}user_profile
            WHERE idam_id IN (:userIds) AND idam_status IS DISTINCT FROM 'DELETED'
            """, nativeQuery = true)
    List<UserProfileMatch> matchProfiles(@Param("userIds") List<String> userIds, @Param("pattern") String pattern);

    @Transactional(readOnly = true)
    Optional<UserProfile> findByEmail(String email);

    @Transactional(readOnly = true)
    Optional<UserProfile> findByIdamId(String id);

    @Transactional(readOnly = true)
    Optional<List<UserProfile>> findByIdamIdInAndStatusNot(List<String> userIds, IdamStatus idamStatus);

    @Transactional(readOnly = true)
    Optional<List<UserProfile>> findByIdamIdIn(List<String> userIds);

    @Transactional(readOnly = true)
    List<UserProfile> findByEmailIgnoreCaseContaining(String emailPattern);

    @Transactional(readOnly = true)
    List<UserProfileIdamStatus> findByUserCategory(UserCategory userCategory);
}
