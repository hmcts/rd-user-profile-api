package uk.gov.hmcts.reform.userprofileapi.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;

import java.util.List;
import java.util.Optional;

import static com.nimbusds.oauth2.sdk.util.CollectionUtils.isNotEmpty;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.constants.TestConstants.COMMON_EMAIL_PATTERN;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ExtendWith(SpringExtension.class)
class UserProfileRepositoryTest {

    @Autowired
    UserProfileRepository userProfileRepository;

    UserProfile userProfile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);

    @BeforeEach
    public void setUp() {
        userProfileRepository.save(userProfile);
    }

    @Test
    void testFindAll() {
        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();

        assertThat(userProfiles).hasSize(1);
        assertThat(userProfiles.iterator().next()).isEqualTo(userProfile);
    }

    @Test
    void testFindByEmail() {
        Optional<UserProfile> user = userProfileRepository.findByEmail(userProfile.getEmail());

        Assertions.assertTrue(user.isPresent());
        assertThat(user).contains(userProfile);
    }

    @Test
    void testFindByIdamId() {
        Optional<UserProfile> user = userProfileRepository.findByIdamId(userProfile.getIdamId());

        Assertions.assertTrue(user.isPresent());
        assertThat(user).contains(userProfile);
    }

    @Test
    void findByEmailIgnoreCaseContaining() {

        List<UserProfile> users = userProfileRepository.findByEmailIgnoreCaseContaining(COMMON_EMAIL_PATTERN);

        Assertions.assertTrue(isNotEmpty(users));
        assertThat(users).contains(userProfile);
    }
}
