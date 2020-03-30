package uk.gov.hmcts.reform.userprofileapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;

public interface AuditRepository extends JpaRepository<Audit, Long> {
}
