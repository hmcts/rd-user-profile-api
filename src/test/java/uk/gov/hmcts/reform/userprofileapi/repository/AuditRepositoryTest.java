package uk.gov.hmcts.reform.userprofileapi.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

@DataJpaTest
@ExtendWith(SpringExtension.class)
class AuditRepositoryTest {

    @Autowired
    AuditRepository auditRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

    @BeforeEach
    public void setUp() {
        userProfileRepository.save(userProfile);
        auditRepository.save(new Audit(1, "test", ResponseSource.API, userProfile));
    }

    @Test
    void testFindAll() {
        List<Audit> audits = auditRepository.findAll();

        assertThat(audits).hasSize(1);
        assertThat(audits.get(0).getUserProfile()).isEqualTo(userProfile);
        assertThat(audits.get(0).getIdamRegistrationResponse()).isEqualTo(1);
        assertThat(audits.get(0).getStatusMessage()).isEqualTo("test");
        assertThat(audits.get(0).getSource()).isEqualTo(ResponseSource.API);
    }
}
