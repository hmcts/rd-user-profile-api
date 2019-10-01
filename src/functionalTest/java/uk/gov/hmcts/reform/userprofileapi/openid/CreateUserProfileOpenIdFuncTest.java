package uk.gov.hmcts.reform.userprofileapi.openid;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.reform.userprofileapi.AbstractFunctional;
import uk.gov.hmcts.reform.userprofileapi.client.IdamOpenIdClient;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;


@RunWith(SpringIntegrationSerenityRunner.class)
public class CreateUserProfileOpenIdFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileOpenIdFuncTest.class);

    @Autowired
    protected TestConfigProperties configProperties;

    private IdamOpenIdClient idamOpenIdClient;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        idamOpenIdClient = new IdamOpenIdClient(configProperties);
    }

    @Test
    public void should_create_user_profile_and_verify_successfully() throws Exception {

        /* CreateUserProfileResponse createdResource = createUserProfile(createUserProfileData(), HttpStatus.CREATED);

        verifyCreateUserProfile(createdResource);*/

        LOG.info("inside create");
        String token = idamOpenIdClient.getBearerToken();

    }
}
