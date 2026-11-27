package uk.gov.hmcts.reform.userprofileapi.service.impl;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileMatchRequest;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class UserProfileMatcherPostgresDeadlineTest {
    @Autowired
    private DataSource datasource;
    @Autowired
    private UserProfileRepository repository;
    @Autowired
    private PlatformTransactionManager transactions;

    @Autowired
    private EntityManager entityManager;
    private UserProfileMatcher matcher;

    @AfterEach
    void closeWorkers() {
        if (matcher != null) {
            matcher.shutdown();
        }
    }

    @Test
    @SuppressWarnings("PMD.PreserveStackTrace") // Unwrap the original worker exception for its HTTP mapping.
    void postgresCancelsLockedSelectWithinRemainingBudgetAndResetsLocalTimeout() throws Exception {
        var jdbc = new JdbcTemplate(datasource);
        var timeoutOnJpaConnection = new AtomicReference<String>();
        var backendPid = new AtomicInteger();
        var observedRepository = mock(UserProfileRepository.class);
        when(observedRepository.matchProfiles(anyList(), anyString())).thenAnswer(invocation -> {
            timeoutOnJpaConnection.set((String) entityManager.createNativeQuery("SHOW statement_timeout")
                    .getSingleResult());
            backendPid.set(((Number) entityManager.createNativeQuery("SELECT pg_backend_pid()")
                    .getSingleResult()).intValue());
            return repository.matchProfiles(invocation.getArgument(0), invocation.getArgument(1));
        });
        matcher = new UserProfileMatcher(observedRepository, transactions, jdbc);
        try (var lock = datasource.getConnection(); var statement = lock.createStatement();
                var caller = Executors.newSingleThreadExecutor()) {
            lock.setAutoCommit(false);
            statement.execute("LOCK TABLE dbuserprofile.user_profile IN ACCESS EXCLUSIVE MODE");
            try {
                var response = caller.submit(() -> matcher.match(
                        new UserProfileMatchRequest(List.of("synthetic-id"), ""),
                        System.currentTimeMillis() + 2500));
                await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                        assertThat(blockedLookups(jdbc, backendPid.get())).isEqualTo(1));
                assertThat(timeoutOnJpaConnection.get()).endsWith("ms");
                assertThat(Long.parseLong(timeoutOnJpaConnection.get().replace("ms", "")))
                        .isPositive().isLessThanOrEqualTo(2500);
                assertThatThrownBy(() -> {
                    try {
                        response.get(3, TimeUnit.SECONDS);
                    } catch (java.util.concurrent.ExecutionException exception) {
                        throw exception.getCause();
                    }
                }).satisfies(error -> {
                    if (error instanceof ResponseStatusException responseError) {
                        assertThat(responseError.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT);
                    } else {
                        assertThat(error).isInstanceOf(org.springframework.dao.QueryTimeoutException.class);
                    }
                });
                // Observe the actual lock waiter; allow local Docker scheduling and rollback to finish.
                await().atMost(Duration.ofMillis(500)).untilAsserted(() ->
                        assertThat(blockedLookups(jdbc, backendPid.get())).isZero());
            } finally {
                lock.rollback();
            }
        }
        assertThat(jdbc.queryForObject("SHOW statement_timeout", String.class)).isEqualTo("0");
        assertThat(matcher.match(new UserProfileMatchRequest(List.of("synthetic-id"), ""),
                        System.currentTimeMillis() + 2000).getUnavailableUserIds()).containsExactly("synthetic-id");
    }

    private int blockedLookups(JdbcTemplate jdbc, int backendPid) {
        return jdbc.queryForObject("""
                SELECT count(*) FROM pg_locks WHERE pid = ? AND NOT granted
                """, Integer.class, backendPid);
    }
}
