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
import uk.gov.hmcts.reform.userprofileapi.client.IdamClient;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;


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


        UserProfileCreationData data = createUserProfileData();
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

        UserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        UserProfileResponse.class
                );

        log.info("before addroles call");
        UserProfileRolesResponse resource1 =
                testRequestHandler.sendPut(
                        userRProfileData,
                            HttpStatus.OK,
                           requestUri + "/" + resource.getIdamId(), UserProfileRolesResponse.class);

        log.info("after addroles call" + resource1);

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );
        log.info("Roles addroles call" + resource2);
        assertThat(resource2.getRoles().size()).isNotNull();
        assertThat(resource2.getRoles().size()).isEqualTo(3);
        assertThat(resource2.getRoles().contains("caseworker,pui-case-manager,pui-user-manager"));

    }

    @Test
    public void rdcc_418_ac1_should_update_user_status_from_active_to_suspended() throws Exception {
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
        userProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UserProfileRolesResponse updatedStatusResponse =
                testRequestHandler.sendPut(
                        userProfileData,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileRolesResponse.class);

        UserProfileWithRolesResponse actual =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );

        assertThat(updatedStatusResponse).isNotNull();

        assertThat(actual).isNotNull();

        assertThat(actual.getIdamId()).isNotNull();
        log.info("retrieved user with updated status for idamId:" + actual.getIdamId());

        assertThat(actual.getIdamStatus()).isEqualTo(IdamStatus.SUSPENDED.name());
        log.info("user updated to:" + actual.getIdamStatus());
    }

    @Test
    public void rdcc_418_ac2_should_update_user_status_from_suspended_to_active() throws Exception {
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
        userProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        UserProfileRolesResponse updatedStatusResponse =
                testRequestHandler.sendPut(
                        userProfileData,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileRolesResponse.class);

        UserProfileWithRolesResponse actual =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );

        assertThat(updatedStatusResponse).isNotNull();

        assertThat(actual).isNotNull();

        assertThat(actual.getIdamId()).isNotNull();
        log.info("retrieved user with updated status for idamId:" + actual.getIdamId());

        assertThat(actual.getIdamStatus()).isEqualTo(IdamStatus.SUSPENDED.name());
        log.info("user updated to:" + actual.getIdamStatus());

        //making same user to ACTIVE from SUSPENDED

        UpdateUserProfileData userProfileData1 = new UpdateUserProfileData();
        userProfileData1.setIdamStatus(IdamStatus.ACTIVE.name());
        UserProfileRolesResponse updatedStatusResponse1 =
                testRequestHandler.sendPut(
                        userProfileData1,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId() + "?origin=exui", UserProfileRolesResponse.class);

        UserProfileWithRolesResponse actual1 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );

        assertThat(updatedStatusResponse1).isNotNull();

        assertThat(actual1).isNotNull();

        assertThat(actual1.getIdamId()).isNotNull();
        log.info("retrieved user with updated status for idamId:" + actual1.getIdamId());

        assertThat(actual1.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
        log.info("user updated to:" + actual1.getIdamStatus());
    }

}
