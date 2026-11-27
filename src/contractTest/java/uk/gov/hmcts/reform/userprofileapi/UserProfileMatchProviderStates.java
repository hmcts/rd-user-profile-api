package uk.gov.hmcts.reform.userprofileapi;

import au.com.dius.pact.provider.junitsupport.State;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileMatchController;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileMatch;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileMatcher;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class UserProfileMatchProviderStates {
    private final UserProfileRepository repository = mock(UserProfileRepository.class);
    private final UserProfileMatcher matcher = new UserProfileMatcher(repository,
            mock(PlatformTransactionManager.class), mock(JdbcTemplate.class));

    UserProfileMatchController controller() {
        return new UserProfileMatchController(matcher);
    }

    void closeMatcher() {
        matcher.shutdown();
    }

    @State("Scoped name match classifies match nonmatch and unavailable")
    public void nameMatch() {
        profiles("%smith%", true, false);
    }

    @State("Scoped page display partitions all requested ids")
    public void pageDisplay() {
        profiles("%%", true, true);
    }

    @State("Scoped name match returns successful zero matches")
    public void noMatches() {
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
