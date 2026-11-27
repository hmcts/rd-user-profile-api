package uk.gov.hmcts.reform.userprofileapi.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserProfileMatchRepositoryTest {
    @Autowired
    private UserProfileRepository repository;
    @Autowired
    private EntityManager entityManager;

    @Test
    void matchesCaseInsensitiveWholeNameAndKeepsEligibleNonmatchesForClassification() {
        save("pending", "Ann", "Smith", IdamStatus.PENDING);
        save("suspended", "ANN", "SMITH", IdamStatus.SUSPENDED);
        save("deleted", "Ann", "Smith", IdamStatus.DELETED);
        save("nonmatch", "Anne", "Jones", IdamStatus.ACTIVE);
        save("outside", "Ann", "Smith", IdamStatus.ACTIVE);
        entityManager.flush();
        entityManager.createNativeQuery("SET LOCAL search_path TO public").executeUpdate();
        var rows = repository.matchProfiles(List.of("pending", "suspended", "deleted", "nonmatch", "missing"),
                "%n sMi%");
        assertThat(rows).extracting(UserProfileMatch::getUserIdentifier)
                .containsExactlyInAnyOrder("pending", "suspended", "nonmatch");
        assertThat(rows).filteredOn(UserProfileMatch::getNameMatches)
                .extracting(UserProfileMatch::getUserIdentifier).containsExactlyInAnyOrder("pending", "suspended");
        assertThat(rows).filteredOn(p -> p.getUserIdentifier().equals("pending"))
                .extracting(UserProfileMatch::getEmail).containsExactly("pending@example.test");
    }

    @Test
    void likeWildcardsEscapeCharacterAndSqlSyntaxAreLiteral() {
        save("literal", "A%_!\\'", "Smith", IdamStatus.ACTIVE);
        save("control", "Anything", "Smith", IdamStatus.ACTIVE);
        var ids = List.of("literal", "control");
        for (String pattern : List.of("%!%!_!!\\'%", "%A!%!_!!\\' S%")) {
            assertThat(repository.matchProfiles(ids, pattern)).filteredOn(UserProfileMatch::getNameMatches)
                    .extracting(UserProfileMatch::getUserIdentifier).containsExactly("literal");
        }
        assertThat(repository.matchProfiles(ids, "%x' OR 1=1 --%"))
                .noneMatch(UserProfileMatch::getNameMatches);
    }

    @Test
    void emptyModeIncludesAllEligibleAndNonemptySpacesAreNotTrimmed() {
        save("single", "Ann", "Smith", IdamStatus.PENDING);
        save("double", "Ann ", "Smith", IdamStatus.SUSPENDED);
        save("deleted", "Ann", "Smith", IdamStatus.DELETED);
        var ids = List.of("single", "double", "deleted", "missing");
        assertThat(repository.matchProfiles(ids, "%%")).allMatch(UserProfileMatch::getNameMatches)
                .extracting(UserProfileMatch::getUserIdentifier).containsExactlyInAnyOrder("single", "double");
        assertThat(repository.matchProfiles(ids, "%  %")).filteredOn(UserProfileMatch::getNameMatches)
                .extracting(UserProfileMatch::getUserIdentifier).containsExactly("double");
    }

    @Test
    void nullStatusIsEligibleRatherThanFalselyUnavailable() {
        save("unknown-status", "Ann", "Smith", null);
        assertThat(repository.matchProfiles(List.of("unknown-status"), "%ann%"))
                .allMatch(UserProfileMatch::getNameMatches).hasSize(1);
    }

    @Test
    void postgresCaseFoldingSupportsAccentedNames() {
        save("accented", "Élodie", "Müller", IdamStatus.ACTIVE);
        assertThat(repository.matchProfiles(List.of("accented"), "%ÉLODIE MÜL%"))
                .allMatch(UserProfileMatch::getNameMatches).hasSize(1);
    }

    private void save(String id, String first, String last, IdamStatus status) {
        UserProfile profile = new UserProfile(buildCreateUserProfileData(), HttpStatus.CREATED);
        profile.setIdamId(id);
        profile.setEmail(id + "@example.test");
        profile.setFirstName(first);
        profile.setLastName(last);
        profile.setStatus(status);
        repository.save(profile);
    }
}
