package uk.gov.hmcts.reform.userprofileapi.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.UserProfileControllerAdvice;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileMatch;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileMatcher;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.json.JsonCompareMode.STRICT;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class UserProfileMatchControllerTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = standaloneSetup(new UserProfileMatchController(new UserProfileMatcher(repository,
            mock(org.springframework.transaction.PlatformTransactionManager.class),
            mock(org.springframework.jdbc.core.JdbcTemplate.class))))
                .setControllerAdvice(new UserProfileControllerAdvice()).build();
    }

    @Test
    void returnsMinimalSnapshotAndExplicitAbsenceWithoutNonmatches() throws Exception {
        UserProfileMatch match = mock(UserProfileMatch.class);
        when(match.getUserIdentifier()).thenReturn("present");
        when(match.getFirstName()).thenReturn("Ann");
        when(match.getLastName()).thenReturn("Smith");
        when(match.getEmail()).thenReturn("synthetic@example.test");
        when(match.getNameMatches()).thenReturn(true);
        UserProfileMatch nonmatch = mock(UserProfileMatch.class);
        when(nonmatch.getUserIdentifier()).thenReturn("nonmatch");
        when(repository.matchProfiles(List.of("present", "nonmatch", "absent"), "%ann%"))
                .thenReturn(List.of(match, nonmatch));
        mvc.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON)
                        .content("{\"userIds\":[\"present\",\"nonmatch\",\"absent\"],\"fullNameContains\":\"ann\"}"))
                .andExpect(status().isOk()).andExpect(content().json("""
                        {"matchedProfiles":[{"userIdentifier":"present","firstName":"Ann","lastName":"Smith",
                        "email":"synthetic@example.test"}],"unavailableUserIds":["absent"]}
                        """, STRICT));
    }

    @Test
    void acceptsEmptyIdentifiersAndEmptySearch() throws Exception {
        mvc.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON)
                        .content("{\"userIds\":[],\"fullNameContains\":\"\"}"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"matchedProfiles\":[],\"unavailableUserIds\":[]}", STRICT));
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMissingNullBlankAndOversizedIdentifierFields() throws Exception {
        var json = new com.fasterxml.jackson.databind.ObjectMapper();
        for (String invalid : List.of("{\"userIds\":[1],\"fullNameContains\":\"a\"}",
                "{\"userIds\":[true],\"fullNameContains\":\"a\"}",
                "{\"userIds\":[],\"fullNameContains\":123}",
                "{\"userIds\":[],\"fullNameContains\":false}", "{", "[]", "", "{}",
                "{\"userIds\":[],\"fullNameContains\":null}",
                "{\"userIds\":null,\"fullNameContains\":\"a\"}",
                "{\"userIds\":[null],\"fullNameContains\":\"a\"}",
                "{\"userIds\":[\"  \"],\"fullNameContains\":\"a\"}",
                "{\"userIds\":[\"" + "a".repeat(51) + "\"],\"fullNameContains\":\"a\"}",
                json.writeValueAsString(java.util.Map.of("userIds", Collections.nCopies(1001, "id"),
                        "fullNameContains", "a")))) {
            mvc.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON).content(invalid))
                    .andExpect(status().isBadRequest());
        }
        verifyNoInteractions(repository);
    }

    @Test
    void acceptsThousandRawIdsAndFiftyCharacterNonUuidIdsWithoutTrimmingTerm() throws Exception {
        var id = "a".repeat(50);
        when(repository.matchProfiles(List.of(id), "% %")).thenReturn(List.of());
        var json = new com.fasterxml.jackson.databind.ObjectMapper();
        mvc.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON)
                        .content(json.writeValueAsString(java.util.Map.of("userIds", Collections.nCopies(1000, id),
                                "fullNameContains", " "))))
                .andExpect(status().isOk()).andExpect(content().json("{\"matchedProfiles\":[],"
                        + "\"unavailableUserIds\":[\"" + id + "\"]}", STRICT));
    }

    @Test
    void rejectsTrailingJsonTokens() throws Exception {
        for (String trailing : List.of("{}", "true", "garbage")) {
            mvc.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON)
                            .content("{\"userIds\":[],\"fullNameContains\":\"\"}" + trailing))
                    .andExpect(status().isBadRequest()).andExpect(content().string(""));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsMalformedAndExpiredDeadlinesBeforeLookup() throws Exception {
        for (String deadline : List.of("not-a-number", "-1")) {
            mvc.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON)
                            .header("X-Organisation-Search-Deadline", deadline)
                            .content("{\"userIds\":[\"id\"],\"fullNameContains\":\"\"}"))
                    .andExpect(status().is("-1".equals(deadline) ? 504 : 400))
                    .andExpect(content().string(""));
        }
        verifyNoInteractions(repository);
    }

    @Test
    void dependencyErrorsReturnControlledStatusWithoutExposingRejectedValues() throws Exception {
        var matcher = mock(UserProfileMatcher.class);
        var isolated = standaloneSetup(new UserProfileMatchController(matcher))
                .setControllerAdvice(new UserProfileControllerAdvice()).build();
        for (var exception : List.of(new org.springframework.dao.DataAccessResourceFailureException("private-value"),
                new org.springframework.dao.QueryTimeoutException("private-value"))) {
            org.mockito.Mockito.doThrow(exception).when(matcher)
                    .match(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
            isolated.perform(post("/v1/userprofile/users/match").contentType(APPLICATION_JSON)
                            .content("{\"userIds\":[\"id\"],\"fullNameContains\":\"private-value\"}"))
                    .andExpect(status().is(
                            exception instanceof org.springframework.dao.QueryTimeoutException ? 504 : 503))
                    .andExpect(content().string(""));
        }
    }

}
