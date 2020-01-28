package uk.gov.hmcts.reform.userprofileapi.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

@DataJpaTest
@RunWith(SpringRunner.class)
public class UserProfileRepositoryTest {

    @Autowired
    UserProfileRepository userProfileRepository;

    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

    @Before
    public void setUp() {
        userProfileRepository.save(userProfile);
    }

    @Test
    public void findAllTest() {
        assertThat(userProfileRepository.findAll()).hasSize(1);
    }

    @Test
    public void findByEmailTest() {
        assertTrue(userProfileRepository.findByEmail(userProfile.getEmail()).isPresent());
    }

    @Test
    public void findByIdamIdTest() {
        assertTrue(userProfileRepository.findByIdamId(userProfile.getIdamId()).isPresent());
    }
}
