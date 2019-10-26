package uk.gov.hmcts.reform.userprofileapi;

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
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
public class DeleteRolesToExistingUserFuncTest extends AbstractFunctional {

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
    public void should_delete_user_profile_with_roles_successfully() throws Exception {
        final String firstName = "April";
        final String lastName = "O'Neil";

        UserProfileCreationData data = createUserProfileData();
        data.setFirstName(firstName);//TODO tbc if required for update
        data.setLastName(lastName);//TODO tbc if requried for update
        data.setStatus(IdamStatus.ACTIVE);//TODO tbc if requried for update

        List<String> roles = new ArrayList<>();
        roles.add(/*puiUserManager*/"pui-user-manager");
        roles.add("pui-case-manager");
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        data.setEmailCommsConsent(false);
        data.setLanguagePreference("EN");
        data.setPostalCommsConsent(false);
        data.setRoles(roles);

        UserProfileCreationResponse dataTmp = createUserProfile(data, HttpStatus.CREATED);
        log.info("UserProfileCreationResponse:" + dataTmp);


        //Roles to add
        Set<RoleName> rolesName = new HashSet<>();
        UpdateUserProfileData userProfileData = new UpdateUserProfileData();
        userProfileData.setEmail(email);
        userProfileData.setFirstName(firstName);
        userProfileData.setLastName(lastName);
        userProfileData.setIdamStatus(IdamStatus.SUSPENDED.name());
        userProfileData.setRolesAdd(rolesName);
        Set<RoleName> rolesDelete = new HashSet<>();

        RoleName role1 = new RoleName(/*puiCaseManager*/"pui-user-manager");
        rolesDelete.add(role1);

        userProfileData.setRolesDelete(rolesDelete);

        log.info("updating user with payload:" + userProfileData);

        UserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + email.toLowerCase(),
                        UserProfileResponse.class
                );

        log.info("get resp:" + resource);

        log.info("should_update_user_profile_with_roles_successfully::before addroles call");
        UserProfileResponse resource1 =
                testRequestHandler.sendPut(
                        userProfileData,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId(), UserProfileResponse.class);

        log.info("after addroles call" + resource1);
        /*UserProfileCreationData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        String email = idamClient.createUser(roles);

        data.setEmail(email);
        createUserProfile(data, HttpStatus.CREATED);

        RoleName role1 = new RoleName(puiOrgManager);
        Set<RoleName> rolesName = new HashSet<>();
        rolesName.add(role1);
        UpdateUserProfileData userProfileData = new UpdateUserProfileData();
        userProfileData.setRolesAdd(rolesName);

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

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );
        log.info("Roles addroles call" + resource2);
        assertThat(resource2.getRoles().size()).isNotNull();
        assertThat(resource2.getRoles().size()).isEqualTo(3);
        RoleName roleDelete = new RoleName(puiOrgManager);
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleDelete);

        UpdateUserProfileData userProfileDataDelete = new UpdateUserProfileData();
        userProfileDataDelete.setRolesDelete(rolesDelete);

        UserProfileResponse deleteResourceResp =
                testRequestHandler.sendDelete(
                        userProfileDataDelete,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId(), UserProfileResponse.class);

        log.info("after DeleteRole call" + deleteResourceResp);


        UserProfileWithRolesResponse resourceForDeleteCheck =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );
        log.info("Roles addroles call" + resource2);
        assertThat(resourceForDeleteCheck.getRoles().size()).isNotNull();
        assertThat(resourceForDeleteCheck.getRoles().size()).isEqualTo(2);
        assertThat(resourceForDeleteCheck.getRoles().contains("caseworker,pui-user-manager"));
        assertThat(!resourceForDeleteCheck.getRoles().contains(puiOrgManager));
        */
    }
}
