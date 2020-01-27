package uk.gov.hmcts.reform.userprofileapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface AuditRepository extends JpaRepository<Audit, Long> {

    @Transactional(readOnly = true)
    Optional<Audit> findByUserProfile(UserProfile userProfile);

}
