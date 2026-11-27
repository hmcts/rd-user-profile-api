package uk.gov.hmcts.reform.userprofileapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileMatchController;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileMatch;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileMatcher;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Provider("rd_user_profile_api_service")
@PactFolder("src/contractTest/pacts/profile-match")
class UserProfileMatchProviderTest {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final UserProfileMatcher matcher = new UserProfileMatcher(repository,
            mock(PlatformTransactionManager.class), mock(JdbcTemplate.class));

    @BeforeEach
    void target(PactVerificationContext context) {
        var target = new MockMvcTestTarget();
        target.setControllers(new UserProfileMatchController(matcher));
        context.setTarget(target);
    }

    @AfterEach
    void closeMatcher() {
        matcher.shutdown();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @State("Scoped name match classifies match nonmatch and unavailable")
    void nameMatch() {
        profiles("%smith%", true, false);
    }

    @State("Scoped page display partitions all requested ids")
    void pageDisplay() {
        profiles("%%", true, true);
    }

    @State("Scoped name match returns successful zero matches")
    void noMatches() {
        profiles("%absent%", false, false);
    }

    private void profiles(String pattern, boolean firstMatches, boolean secondMatches) {
        var first = profile("synthetic-a", "Ada", "Smith", "ada@example.invalid", firstMatches);
        var second = profile("synthetic-b", "Grace", "Jones", "grace@example.invalid", secondMatches);
        when(repository.matchProfiles(List.of("synthetic-a", "synthetic-b", "synthetic-c"), pattern))
                .thenReturn(List.of(first, second));
    }

    private UserProfileMatch profile(String id, String first, String last, String email, boolean matches) {
        var profile = mock(UserProfileMatch.class);
        when(profile.getUserIdentifier()).thenReturn(id);
        when(profile.getFirstName()).thenReturn(first);
        when(profile.getLastName()).thenReturn(last);
        when(profile.getEmail()).thenReturn(email);
        when(profile.getNameMatches()).thenReturn(matches);
        return profile;
    }
}
