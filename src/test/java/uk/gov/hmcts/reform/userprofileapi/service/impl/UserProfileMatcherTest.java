package uk.gov.hmcts.reform.userprofileapi.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileMatchRequest;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileMatch;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class UserProfileMatcherTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final org.springframework.transaction.PlatformTransactionManager transactions =
            mock(org.springframework.transaction.PlatformTransactionManager.class);
    private final UserProfileMatcher matcher = new UserProfileMatcher(repository, transactions,
            mock(org.springframework.jdbc.core.JdbcTemplate.class));

    @AfterEach
    void closeWorkers() {
        matcher.shutdown();
    }

    @Test
    void classifiesMatchesNonmatchesAndUnavailableFromOneSnapshot() {
        var profiles = List.of(profile("present", true), profile("nonmatch", false));
        when(repository.matchProfiles(List.of("present", "nonmatch", "missing", "deleted"), "%ann%"))
                .thenReturn(profiles);

        var response = matcher.match(new UserProfileMatchRequest(
                List.of("present", "nonmatch", "missing", "deleted", "present"), "ann"), null);

        assertThat(response.getMatchedProfiles()).extracting("userIdentifier").containsExactly("present");
        assertThat(response.getMatchedProfiles().getFirst().getFirstName()).isEqualTo("Ann");
        assertThat(response.getUnavailableUserIds()).containsExactly("missing", "deleted");
        verify(repository).matchProfiles(List.of("present", "nonmatch", "missing", "deleted"), "%ann%");
        verifyNoMoreInteractions(repository);
    }

    @Test
    void escapesLikeMetacharactersAndPreservesSpacesAndCaseForDatabaseCaseFolding() {
        when(repository.matchProfiles(List.of("id"), "% A!%!_!!\\ %")).thenReturn(List.of());
        matcher.match(new UserProfileMatchRequest(List.of("id"), " A%_!\\ "), null);
        verify(repository).matchProfiles(List.of("id"), "% A!%!_!!\\ %");
    }

    @Test
    void emptyTermUsesAllEligibleSnapshots() {
        var profiles = List.of(profile("id", true));
        when(repository.matchProfiles(List.of("id", "absent"), "%%"))
                .thenReturn(profiles);
        var response = matcher.match(new UserProfileMatchRequest(List.of("id", "absent"), ""), null);
        assertThat(response.getMatchedProfiles()).hasSize(1);
        assertThat(response.getUnavailableUserIds()).containsExactly("absent");
    }

    @Test
    void emptyIdentifiersReturnEmptyWithoutQuery() {
        var response = matcher.match(new UserProfileMatchRequest(List.of(), "anything"), null);
        assertThat(response.getMatchedProfiles()).isEmpty();
        assertThat(response.getUnavailableUserIds()).isEmpty();
        verifyNoInteractions(repository, transactions);
    }

    @Test
    void expiredDeadlineFailsBeforeDatabaseWorkEvenForEmptyIds() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> matcher.match(
                new UserProfileMatchRequest(List.of(), ""), System.currentTimeMillis() - 1))
                .isInstanceOf(org.springframework.web.server.ResponseStatusException.class);
        verifyNoInteractions(repository, transactions);
    }

    @Test
    void clampsTransactionTimeoutToTwentyFiveSeconds() {
        matcher.match(new UserProfileMatchRequest(List.of("id"), ""), Long.MAX_VALUE);
        verify(transactions).getTransaction(org.mockito.ArgumentMatchers.argThat(
                definition -> definition.isReadOnly() && definition.getTimeout() == 25));
    }

    @Test
    void appliesRemainingBudgetToTransaction() {
        matcher.match(new UserProfileMatchRequest(List.of("id"), ""), System.currentTimeMillis() + 2200);
        verify(transactions).getTransaction(org.mockito.ArgumentMatchers.argThat(
                definition -> definition.getTimeout() > 0 && definition.getTimeout() <= 3));
    }

    private UserProfileMatch profile(String id, boolean matches) {
        UserProfileMatch profile = mock(UserProfileMatch.class);
        when(profile.getUserIdentifier()).thenReturn(id);
        when(profile.getNameMatches()).thenReturn(matches);
        if (matches) {
            when(profile.getFirstName()).thenReturn("Ann");
            when(profile.getLastName()).thenReturn("Smith");
            when(profile.getEmail()).thenReturn("synthetic@example.test");
        }
        return profile;
    }
}
