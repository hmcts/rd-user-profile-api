package uk.gov.hmcts.reform.userprofileapi;



import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Ignore
@RunWith(SpringIntegrationSerenityRunner.class)
public class AddRolesFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(CreateUserProfileFuncTest.class);

    @Autowired
    protected TestConfigProperties configProperties;

    private IdamClient idamClient;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        idamClient = new IdamClient(configProperties);
    }

    @Test
    public void should_create_user_profile_with_roles_successfully() throws Exception {

        String email = idamClient.createUser("pui-user-manager");
        CreateUserProfileData data = createUserProfileData();
        data.setEmail(email);
        CreateUserProfileResponse UserResource = createUserProfile(data, HttpStatus.CREATED);


        GetUserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        GetUserProfileResponse.class
                );

        AddRoleName role1 = new AddRoleName("pui-case-manager");
        AddRoleName role2 = new AddRoleName("prd-Admin");

        List<AddRoleName> roles = new ArrayList<>();
        roles.add(role1);
        roles.add(role2);

              testRequestHandler.sendPost(
                      roles,
                HttpStatus.OK,
                requestUri + "/users/"+ resource.getIdamId() + "/roles",
                CreateUserProfileResponse.class
        );

        GetUserProfileWithRolesResponse resource1 =
                testRequestHandler.sendGet(
                        requestUri + "/roles?email=" + email.toLowerCase(),
                        GetUserProfileWithRolesResponse.class
                );

        assertThat(resource1.getRoles().size()).isNotNull();
        assertThat(resource1.getRoles().size()).isEqualTo(3);
    }
}
