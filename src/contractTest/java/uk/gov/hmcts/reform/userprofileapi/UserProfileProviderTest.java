package uk.gov.hmcts.reform.userprofileapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileController;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

import static org.mockito.Mockito.doReturn;

import java.util.Collections;
import java.util.List;
import javax.sql.DataSource;

@ExtendWith(SpringExtension.class)
@Provider("rd_user_profile_service")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
        port = "${PACT_BROKER_PORT:9292}")
@Import(UserProfileProviderTestConfiguration.class)
@SpringBootTest(properties = {"crd.publisher.caseWorkerDataPerMessage=1"})
public class UserProfileProviderTest {

    @Autowired
    private UserProfileService<RequestData> userProfileService;

    @Autowired
    private IdamService idamService;

    @Autowired
    private ValidationService validationService;

//    @Autowired
//    private DataSource ds;

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new UserProfileController(userProfileService, idamService, validationService));
        context.setTarget(testTarget);
    }

    @State({"A user profile create request is submitted"})
    public void createUserProfile() {
        //doReturn(caseWorkerProfile).when(caseWorkerProfileRepo).findByCaseWorkerIdIn(userRequest);
    }

    @State({"A user profile get request is submitted"})
    public void getUserProfile() {
        //empty
    }

    @State({"A user profile update request is submitted"})
    public void updateUserProfile() {
        //empty
    }
}
