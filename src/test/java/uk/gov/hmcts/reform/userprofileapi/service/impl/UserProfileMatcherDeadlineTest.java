package uk.gov.hmcts.reform.userprofileapi.service.impl;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileMatchRequest;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;

import java.sql.Connection;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserProfileMatcherDeadlineTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final UserProfileMatchRequest request = new UserProfileMatchRequest(List.of("synthetic-id"), "");

    private UserProfileMatcher matcher;

    @AfterEach
    void closeWorkers() {
        if (matcher != null) {
            matcher.shutdown();
        }
    }

    @Test
    void deadlineInterruptsRealHikariAcquisitionAndRecovers() throws Exception {
        var datasource = mock(DataSource.class);
        @SuppressWarnings("PMD.CloseResource") // Hikari owns and closes this synthetic physical connection.
        var connection = mock(Connection.class);
        when(connection.isValid(anyInt())).thenReturn(true);
        when(datasource.getConnection()).thenReturn(connection);
        var config = new HikariConfig();
        config.setDataSource(datasource);
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(30_000);
        try (var pool = new HikariDataSource(config); var heldConnection = pool.getConnection()) {
            matcher = new UserProfileMatcher(repository, new DataSourceTransactionManager(pool),
                    mock(org.springframework.jdbc.core.JdbcTemplate.class));
            assertTimeoutPreemptively(Duration.ofSeconds(2), () -> assertThatThrownBy(() ->
                    matcher.match(request, System.currentTimeMillis() + 300))
                    .isInstanceOfSatisfying(ResponseStatusException.class,
                        error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT)));
            await().atMost(Duration.ofSeconds(2)).untilAsserted(() ->
                    assertThat(pool.getHikariPoolMXBean().getThreadsAwaitingConnection()).isZero());
            verifyNoInteractions(repository);
            heldConnection.close();
            assertThat(matcher.match(request, System.currentTimeMillis() + 2000)
                    .getUnavailableUserIds()).containsExactly("synthetic-id");
        }
    }

    @Test
    void boundedAdmissionHoldsCapacityUntilCancelledWorkActuallyExits() throws Exception {
        var transactions = mock(PlatformTransactionManager.class);
        var admitted = new CountDownLatch(32);
        var release = new CountDownLatch(1);
        when(transactions.getTransaction(any())).thenAnswer(invocation -> {
            admitted.countDown();
            while (release.getCount() != 0) {
                try {
                    release.await();
                } catch (InterruptedException ignored) {
                    // Simulate a JDBC driver which ignores thread interruption.
                }
            }
            return null;
        });
        matcher = new UserProfileMatcher(repository, transactions,
                mock(org.springframework.jdbc.core.JdbcTemplate.class));
        try (var callers = Executors.newFixedThreadPool(32)) {
            try {
                var results = new ArrayList<Future<?>>();
                for (int index = 0; index < 32; index++) {
                    results.add(callers.submit(() -> assertThatThrownBy(() -> matcher.match(request,
                            System.currentTimeMillis() + 1000)).isInstanceOf(ResponseStatusException.class)));
                }
                assertThat(admitted.await(2, TimeUnit.SECONDS)).isTrue();
                assertTimeoutPreemptively(Duration.ofSeconds(2), () -> assertThatThrownBy(() ->
                        matcher.match(request, System.currentTimeMillis() + 2000))
                        .isInstanceOfSatisfying(ResponseStatusException.class,
                            error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE)));
                callers.shutdown();
                assertThat(callers.awaitTermination(2, TimeUnit.SECONDS)).isTrue();
                for (var result : results) {
                    result.get();
                }
                assertThatThrownBy(() -> matcher.match(request, System.currentTimeMillis() + 2000))
                        .isInstanceOfSatisfying(ResponseStatusException.class,
                            error -> assertThat(error.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE));
                verifyNoInteractions(repository);
            } finally {
                release.countDown();
            }
        }
        await().atMost(Duration.ofSeconds(2)).untilAsserted(() -> assertThat(
                matcher.match(request, System.currentTimeMillis() + 1000).getUnavailableUserIds())
                .containsExactly("synthetic-id"));
    }
}
