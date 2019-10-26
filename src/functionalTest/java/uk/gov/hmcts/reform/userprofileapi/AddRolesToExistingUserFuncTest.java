package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.response.*;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.*;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class AddRolesToExistingUserFuncTest extends AbstractFunctional {

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
        final String firstName = "April";
        final String lastName = "O'Neil";

        UserProfileCreationData data = createUserProfileData();
        data.setFirstName(firstName);//TODO tbc if required for update
        data.setLastName(lastName);//TODO tbc if requried for update
        data.setStatus(IdamStatus.ACTIVE);//TODO tbc if requried for update

        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        UserProfileCreationResponse dataTmp = createUserProfile(data, HttpStatus.CREATED);

        RoleName role1 = new RoleName(/*puiCaseManager*/"pui-case-manager");
        Set<RoleName> rolesName = new HashSet<>();
        rolesName.add(role1);
        UpdateUserProfileData userProfileData = new UpdateUserProfileData();
        userProfileData.setEmail(email);
        userProfileData.setFirstName(firstName);
        userProfileData.setLastName(lastName);
        userProfileData.setIdamStatus(IdamStatus.ACTIVE.name());
        userProfileData.setRolesAdd(rolesName);

        log.info("updating user with payload:" + userProfileData);

        UserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        UserProfileResponse.class
                );

        log.info("before addroles call");
        UserProfileResponse resource1 =
                testRequestHandler.sendPut(
                        userProfileData,
                            HttpStatus.OK,
                           requestUri + "/" + resource.getIdamId(), UserProfileResponse.class);

        log.info("after addroles call" + resource1);
        assertThat(resource1.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE);
        assertThat(resource1.getAddRolesResponse()).isNotNull();
        assertThat(resource1.getAddRolesResponse().getIdamMessage()).isNotNull();
        assertThat(resource1.getAddRolesResponse().getIdamStatusCode()).isNotNull();


        //TODO remove this obj!
        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );
        log.info("Roles addroles call" + resource2);
        //!? assertThat(resource2.getRoles().size()).isNotNull();
        //!? assertThat(resource2.getRoles().size()).isEqualTo(3);
        //!? assertThat(resource2.getRoles().contains("caseworker,pui-case-manager,pui-user-manager"));

    }

    @Test
    public void rdcc_418_1_should_update_user_status_from_active_to_suspended() throws Exception {
        UserProfileCreationData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        createUserProfile(data, HttpStatus.CREATED);
        UserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        UserProfileResponse.class
                );

        log.info(String.format("created and retrieved user with email:[%s]", resource.getEmail()));

        //update from active to suspended
        UpdateUserProfileData userProfileData = new UpdateUserProfileData();
        userProfileData.setFirstName("firstName");
        userProfileData.setLastName("lastName");
        userProfileData.setEmail(email);
        userProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UserProfileResponse updatedStatusResponse =
                testRequestHandler.sendPut(
                        userProfileData,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileResponse.class);

        UserProfileResponse actual =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        UserProfileResponse.class
                );

        assertThat(actual.getIdamId()).isNotNull();
        log.info("retrieved user with updated status for idamId:" + actual.getIdamId());

        assertThat(actual.getIdamStatus()).isEqualTo(IdamStatus.SUSPENDED.name());
        log.info("user updated to:" + actual.getIdamStatus());
    }

    //@Test
    public void rdcc_418_2_should_update_user_status_from_suspended_to_active() throws Exception {
        UserProfileCreationData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        createUserProfile(data, HttpStatus.CREATED);
        UserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        UserProfileResponse.class
                );

        log.info("get Userprofile response::" + resource);
        log.info("before addroles call");
        UpdateUserProfileData userRProfileData = new UpdateUserProfileData();
        userRProfileData.setFirstName("firstName");
        userRProfileData.setLastName("lastName");
        userRProfileData.setEmail(email);
        userRProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UserProfileResponse updatedStatusResponse =
                testRequestHandler.sendPut(
                        userRProfileData,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileResponse.class);

        log.info("after Status update call" + updatedStatusResponse);
    }
}
