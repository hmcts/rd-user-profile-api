package uk.gov.hmcts.reform.userprofileapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;

@DataJpaTest
@RunWith(SpringRunner.class)
public class AuditRepositoryTest {

    @Autowired
    AuditRepository auditRepository;

    @Autowired
    UserProfileRepository userProfileRepository;

    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

    @Before
    public void setUp() {
        userProfileRepository.save(userProfile);
        auditRepository.save(new Audit(1, "test", ResponseSource.API, userProfile));
    }

    @Test
    public void findAllTest() {
        List<Audit> audits = auditRepository.findAll();

        assertThat(audits.size()).isEqualTo(1);
        assertThat(audits.get(0).getUserProfile()).isEqualTo(userProfile);
        assertThat(audits.get(0).getIdamRegistrationResponse()).isEqualTo(1);
        assertThat(audits.get(0).getStatusMessage()).isEqualTo("test");
        assertThat(audits.get(0).getSource()).isEqualTo(ResponseSource.API);
    }

    @Test
    public void findByUserProfile() {
        Optional<Audit> audit1 = auditRepository.findByUserProfile(userProfile);

        assertThat(audit1.get().getUserProfile()).isEqualTo(userProfile);
        assertThat(audit1.get().getUserProfile()).isEqualTo(userProfile);
        assertThat(audit1.get().getIdamRegistrationResponse()).isEqualTo(1);
        assertThat(audit1.get().getStatusMessage()).isEqualTo("test");
        assertThat(audit1.get().getSource()).isEqualTo(ResponseSource.API);
    }
}
