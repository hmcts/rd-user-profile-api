package uk.gov.hmcts.reform.userprofileapi.integration.util;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface TestAuditRepository extends JpaRepository<Audit, Long> {

    Optional<Audit> findByUserProfile(UserProfile userProfile);

    List<Audit> findAllByUserProfile(UserProfile userProfile);

}
