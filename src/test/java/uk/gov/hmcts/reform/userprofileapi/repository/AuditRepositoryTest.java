package uk.gov.hmcts.reform.userprofileapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import java.util.List;
import java.util.Optional;

import org.h2.engine.User;
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
        auditRepository.save(new Audit(1, "test", ResponseSource.API, any(UserProfile.class)));

        userProfileRepository.save(userProfile);
    }

    @Test
    public void findAllTest() {
        assertThat(auditRepository.findAll().size()).isEqualTo(1);
    }

    @Test
    public void findByUserProfile() {
        Optional<Audit> audit1 = auditRepository.findByUserProfile(userProfile);

        assertThat(audit1).isNotNull();
    }
}
