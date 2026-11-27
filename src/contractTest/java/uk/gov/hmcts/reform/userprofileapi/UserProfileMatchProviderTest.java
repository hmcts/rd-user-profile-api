package uk.gov.hmcts.reform.userprofileapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;

@Provider("rd_user_profile_api_service")
@PactFolder("src/contractTest/pacts/profile-match")
class UserProfileMatchProviderTest {
    private final UserProfileMatchProviderStates match = new UserProfileMatchProviderStates();

    @BeforeEach
    void target(PactVerificationContext context) {
        var target = new MockMvcTestTarget();
        target.setControllers(match.controller());
        context.setTarget(target);
        context.addStateChangeHandlers(match);
    }

    @AfterEach
    void closeMatcher() {
        match.closeMatcher();
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verifyPact(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
