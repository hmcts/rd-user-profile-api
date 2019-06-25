package uk.gov.hmcts.reform.userprofileapi.infrastructure.repository;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;

public interface AuditRepository extends CrudRepository<Audit, Long> {

}
