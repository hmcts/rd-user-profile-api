package uk.gov.hmcts.reform.userprofileapi.integration.util;

import org.springframework.data.repository.CrudRepository;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

public interface TestUserProfileRepository extends CrudRepository<UserProfile, Long> {
}
