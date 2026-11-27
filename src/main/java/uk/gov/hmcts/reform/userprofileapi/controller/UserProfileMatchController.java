package uk.gov.hmcts.reform.userprofileapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionTimedOutException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileMatchRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileMatchResponse;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileMatcher;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@AllArgsConstructor
public class UserProfileMatchController {
    private final UserProfileMatcher matcher;

    // Validation exceptions contain rejected names/IDs; do not pass them to the shared logging advice.
    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class})
    public ResponseEntity<Void> invalidRequest() {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Void> deadlineExceeded(ResponseStatusException exception) {
        return ResponseEntity.status(exception.getStatusCode()).build();
    }

    @ExceptionHandler({QueryTimeoutException.class, TransactionTimedOutException.class})
    public ResponseEntity<Void> queryTimedOut() {
        return ResponseEntity.status(504).build();
    }

    @ExceptionHandler({DataAccessException.class, TransactionException.class})
    public ResponseEntity<Void> lookupFailed() {
        return ResponseEntity.status(503).build();
    }

    @Operation(summary = "Match requested user profile snapshots by full name",
            security = {@SecurityRequirement(name = "ServiceAuthorization"),
                @SecurityRequirement(name = "Authorization")})
    @PostMapping(path = "/v1/userprofile/users/match", consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE)
    public UserProfileMatchResponse match(@Valid @RequestBody UserProfileMatchRequest request,
            @RequestHeader(value = "X-Organisation-Search-Deadline", required = false) Long deadline) {
        return matcher.match(request, deadline);
    }
}
