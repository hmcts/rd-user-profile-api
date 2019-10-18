package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@RunWith(SpringIntegrationSerenityRunner.class)
public class AddRolesToExistingUserFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(AddRolesToExistingUserFuncTest.class);

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
    public void should_update_user_profile_with_roles_successfully() throws Exception {


        CreateUserProfileData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        createUserProfile(data, HttpStatus.CREATED);

        RoleName role1 = new RoleName(puiCaseManager);
        Set<RoleName> rolesName = new HashSet<>();
        rolesName.add(role1);
        UpdateUserProfileData userRProfileData = new UpdateUserProfileData();
        userRProfileData.setRolesAdd(rolesName);

        GetUserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        GetUserProfileResponse.class
                );

        LOG.info("after addroles call" + resource);
        LOG.info("before addroles call");
        UserProfileRolesResponse resource1 =
                testRequestHandler.sendPut(
                        userRProfileData,
                            HttpStatus.OK,
                           requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileRolesResponse.class);

        GetUserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        GetUserProfileWithRolesResponse.class
                );
        LOG.info("Roles addroles call" + resource2);
        assertThat(resource2.getRoles().size()).isNotNull();
        assertThat(resource2.getRoles().size()).isEqualTo(3);
        assertThat(resource2.getRoles().contains("caseworker,pui-case-manager,pui-user-manager"));

    }

    @Test
    public void should_update_user_status_from_active_to_suspended_in_user_profile_successfully() throws Exception {


        CreateUserProfileData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        createUserProfile(data, HttpStatus.CREATED);
        GetUserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        GetUserProfileResponse.class
                );

        LOG.info("get Userprofile response::" + resource);
        LOG.info("before addroles call");
        UpdateUserProfileData userRProfileData = new UpdateUserProfileData();
        userRProfileData.setFirstName("firstName");
        userRProfileData.setLastName("lastName");
        userRProfileData.setEmail(email);
        userRProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UserProfileRolesResponse updatedStatusResponse =
                testRequestHandler.sendPut(
                        userRProfileData,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileRolesResponse.class);

        LOG.info("after Status update call" + updatedStatusResponse);
    }
}
