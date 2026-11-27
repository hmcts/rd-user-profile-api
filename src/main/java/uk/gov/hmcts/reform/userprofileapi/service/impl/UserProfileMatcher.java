package uk.gov.hmcts.reform.userprofileapi.service.impl;

import jakarta.annotation.PreDestroy;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileMatchRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileMatchResponse;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
public class UserProfileMatcher {
    private static final int MAX_LOOKUP_SECONDS = 25;
    private final UserProfileRepository repository;
    private final JdbcTemplate jdbcTemplate;

    private final PlatformTransactionManager transactionManager;
    // No queue: cancelled JDBC work keeps its slot until it really exits.
    private final ThreadPoolExecutor lookups = new ThreadPoolExecutor(0, 32, 30, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Thread.ofVirtual().name("profile-match-", 0).factory());

    public UserProfileMatcher(UserProfileRepository repository, PlatformTransactionManager transactionManager,
                              JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionManager = transactionManager;
    }

    @SuppressWarnings("PMD.PreserveStackTrace") // Rethrow original worker failures for existing controller mappings.
    public UserProfileMatchResponse match(UserProfileMatchRequest request, Long deadline) {
        long now = System.currentTimeMillis();
        long boundedDeadline = Math.min(deadline == null ? Long.MAX_VALUE : deadline,
                now + MAX_LOOKUP_SECONDS * 1000L);
        if (boundedDeadline <= now) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
        }
        if (request.getUserIds().isEmpty()) {
            return new UserProfileMatchResponse(List.of(), List.of());
        }
        long monotonicDeadline = System.nanoTime() + (boundedDeadline - now) * 1_000_000;
        final Future<UserProfileMatchResponse> lookup;
        try {
            lookup = lookups.submit(() -> {
                checkDeadline(monotonicDeadline);
                var transaction = new TransactionTemplate(transactionManager);
                transaction.setReadOnly(true);
                transaction.setTimeout((int) Math.max(1,
                        TimeUnit.NANOSECONDS.toSeconds(monotonicDeadline - System.nanoTime()) + 1));
                return transaction.execute(status -> matchSnapshot(request, monotonicDeadline));
            });
        } catch (RejectedExecutionException exception) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, null, exception);
        }
        try {
            var response = lookup.get(Math.max(0, monotonicDeadline - System.nanoTime()), TimeUnit.NANOSECONDS);
            checkDeadline(monotonicDeadline);
            return response;
        } catch (TimeoutException exception) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, null, exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT, null, exception);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (exception.getCause() instanceof Error cause) {
                throw cause;
            }
            throw new IllegalStateException(exception);
        } finally {
            lookup.cancel(true);
        }
    }

    @PreDestroy
    public void shutdown() {
        lookups.shutdownNow();
    }

    private UserProfileMatchResponse matchSnapshot(UserProfileMatchRequest request, long deadline) {
        checkDeadline(deadline);
        // Transaction-local: use the acquired transaction connection and its remaining budget.
        jdbcTemplate.queryForObject("SELECT set_config('statement_timeout', ?, true)", String.class,
                Long.toString(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()))));
        var unavailable = new LinkedHashSet<>(request.getUserIds());
        var matched = new ArrayList<UserProfileMatchResponse.Profile>();
        String pattern = "%" + request.getFullNameContains().replace("!", "!!")
                .replace("%", "!%").replace("_", "!_") + "%";
        // Keep nonmatching IDs in this snapshot so absence never triggers stale-name fallback.
        for (var profile : repository.matchProfiles(List.copyOf(unavailable), pattern)) {
            unavailable.remove(profile.getUserIdentifier());
            if (profile.getNameMatches()) {
                matched.add(new UserProfileMatchResponse.Profile(profile.getUserIdentifier(),
                        profile.getFirstName(), profile.getLastName(), profile.getEmail()));
            }
        }
        checkDeadline(deadline);
        return new UserProfileMatchResponse(matched, List.copyOf(unavailable));
    }

    private void checkDeadline(long deadline) {
        if (System.nanoTime() - deadline >= 0) {
            throw new ResponseStatusException(HttpStatus.GATEWAY_TIMEOUT);
        }
    }
}
