package uk.gov.hmcts.reform.userprofileapi.config;

import jakarta.annotation.PreDestroy;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.filters.ServiceAuthFilter;
import uk.gov.hmcts.reform.authorisation.validators.ServiceAuthTokenValidator;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_GATEWAY_TIMEOUT;
import static jakarta.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

@Component
class ProfileMatchAuthorisation {
    static final String DEADLINE_HEADER = "X-Organisation-Search-Deadline";
    private static final String DEADLINE_ATTRIBUTE = ProfileMatchAuthorisation.class.getName();
    private static final int MAX_BUDGET_MILLIS = 25_000;

    // Keep a slot occupied until the authentication call exits, even if its caller has timed out.
    private final ThreadPoolExecutor authorisations = new ThreadPoolExecutor(0, 32, 30, TimeUnit.SECONDS,
            new SynchronousQueue<>(), Thread.ofVirtual().name("profile-match-auth-", 0).factory());

    OncePerRequestFilter serviceFilter(ServiceAuthorisationApi serviceAuthorisationApi) {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                long now = System.currentTimeMillis();
                String header = request.getHeader(DEADLINE_HEADER);
                long deadline;
                try {
                    deadline = Math.min(header == null ? Long.MAX_VALUE : Long.parseLong(header),
                            now + MAX_BUDGET_MILLIS);
                } catch (NumberFormatException exception) {
                    response.setStatus(SC_BAD_REQUEST);
                    return;
                }
                try {
                    if (deadline <= now) {
                        throw new AdmissionFailure(SC_GATEWAY_TIMEOUT);
                    }
                    request.setAttribute(DEADLINE_ATTRIBUTE,
                            System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(deadline - now));
                    var validator = new ServiceAuthTokenValidator(serviceAuthorisationApi) {
                        @Override
                        public String getServiceName(String serviceToken) {
                            return await(request, () -> super.getServiceName(serviceToken));
                        }
                    };
                    new ServiceAuthFilter(validator, List.of("rd_professional_api"))
                            .doFilter(withDeadline(request, Long.toString(deadline)), response, chain);
                } catch (AdmissionFailure exception) {
                    response.setStatus(exception.status);
                }
            }
        };
    }

    @SuppressWarnings("PMD.PreserveStackTrace") // Keep the original authentication exception and status.
    <T> T await(HttpServletRequest request, Supplier<T> operation) {
        long deadline = (long) request.getAttribute(DEADLINE_ATTRIBUTE);
        remainingNanos(deadline);
        try {
            var task = authorisations.submit(() -> {
                remainingNanos(deadline);
                return operation.get();
            });
            try {
                T result = task.get(remainingNanos(deadline), TimeUnit.NANOSECONDS);
                remainingNanos(deadline);
                return result;
            } finally {
                task.cancel(true);
            }
        } catch (RejectedExecutionException exception) {
            throw new AdmissionFailure(SC_SERVICE_UNAVAILABLE);
        } catch (TimeoutException exception) {
            throw new AdmissionFailure(SC_GATEWAY_TIMEOUT);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AdmissionFailure(SC_GATEWAY_TIMEOUT);
        } catch (ExecutionException exception) {
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            if (exception.getCause() instanceof Error cause) {
                throw cause;
            }
            throw new IllegalStateException(exception);
        }
    }

    private static HttpServletRequest withDeadline(HttpServletRequest request, String deadline) {
        return new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                return DEADLINE_HEADER.equalsIgnoreCase(name) ? deadline : super.getHeader(name);
            }

            @Override
            public Enumeration<String> getHeaders(String name) {
                return DEADLINE_HEADER.equalsIgnoreCase(name) ? Collections.enumeration(List.of(deadline))
                        : super.getHeaders(name);
            }
        };
    }

    private static long remainingNanos(long deadline) {
        long remaining = deadline - System.nanoTime();
        if (remaining <= 0) {
            throw new AdmissionFailure(SC_GATEWAY_TIMEOUT);
        }
        return remaining;
    }

    @PreDestroy
    void shutdown() {
        authorisations.shutdownNow();
    }

    private static final class AdmissionFailure extends RuntimeException {
        private final int status;

        private AdmissionFailure(int status) {
            this.status = status;
        }
    }
}
