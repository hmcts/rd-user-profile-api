package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.response.RoleDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
public class DeleteRolesToExistingUserFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(DeleteRolesToExistingUserFuncTest.class);

    @Autowired
    protected TestConfigProperties configProperties;

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_delete_user_profile_with_roles_successfully() throws Exception {
        UserProfileCreationData data = createUserProfileData();
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        Map<String, String> userCreds = idamOpenIdClient.createUser(roles);

        data.setEmail(userCreds.get(EMAIL));
        createUserProfile(data, HttpStatus.CREATED);

        RoleName role1 = new RoleName(puiOrgManager);
        Set<RoleName> rolesName = new HashSet<>();
        rolesName.add(role1);
        UpdateUserProfileData userProfileData = new UpdateUserProfileData();
        userProfileData.setRolesAdd(rolesName);

        UserProfileResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "?email=" + userCreds.get(EMAIL).toLowerCase(),
                        UserProfileResponse.class
                );

        LOG.info("before addroles call");
        UserProfileRolesResponse resource1 =
                testRequestHandler.sendPut(
                        userProfileData,
                            HttpStatus.OK,
                           requestUri + "/" + resource.getIdamId(), UserProfileRolesResponse.class);

        LOG.info("after addroles call" + resource1);

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );
        LOG.info("Roles addroles call" + resource2);
        assertThat(resource2.getRoles().size()).isNotNull();
        assertThat(resource2.getRoles().size()).isEqualTo(3);
        RoleName roleDelete = new RoleName(puiOrgManager);
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleDelete);

        UpdateUserProfileData userProfileDataDelete = new UpdateUserProfileData();
        userProfileDataDelete.setRolesDelete(rolesDelete);

        UserProfileRolesResponse deleteResourceResp =
                testRequestHandler.sendDelete(
                        userProfileDataDelete,
                        HttpStatus.OK,
                        requestUri + "/" + resource.getIdamId(), UserProfileRolesResponse.class);

        LOG.info("after DeleteRole call" + deleteResourceResp);


        UserProfileWithRolesResponse resourceForDeleteCheck =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class
                );
        LOG.info("Roles addroles call" + resource2);
        assertThat(resourceForDeleteCheck.getRoles().size()).isNotNull();
        assertThat(resourceForDeleteCheck.getRoles().size()).isEqualTo(2);
        assertThat(resourceForDeleteCheck.getRoles().contains("caseworker,pui-user-manager"));
        assertThat(!resourceForDeleteCheck.getRoles().contains(puiOrgManager));

    }

    @Test
    public void should_throw_412_while_delete_user_profile_with_invalid_roles_passed() throws Exception {
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        UserProfileCreationResponse userProfileCreationResponse
                = createActiveUserProfileWithGivenRoles(HttpStatus.CREATED, roles);

        RoleName roleDelete = new RoleName("pui-org-manager");
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleDelete);

        UserProfileRolesResponse deleteResourceResp = deleteRoleRequestWithGivenRoles(rolesDelete,
                userProfileCreationResponse.getIdamId());
        verifyDeleteRoleResponse(deleteResourceResp, "One or more of the roles provided does not exist.",
                "pui-org-manager","412");
    }

    @Test
    public void should_throw_412_while_delete_user_profile_with_unassigned_roles_passed() throws Exception {
        List<String> roles = new ArrayList<>();
        roles.add(puiUserManager);
        UserProfileCreationResponse userProfileCreationResponse
                = createActiveUserProfileWithGivenRoles(HttpStatus.CREATED, roles);

        RoleName roleDelete = new RoleName(puiCaseManager);
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(roleDelete);

        UserProfileRolesResponse deleteResourceResp = deleteRoleRequestWithGivenRoles(rolesDelete,
                userProfileCreationResponse.getIdamId());
        verifyDeleteRoleResponse(deleteResourceResp, "The role provided is not assigned to the user.",
                puiCaseManager,"412");
    }

    public void verifyDeleteRoleResponse(UserProfileRolesResponse deleteResourceResp, String errorMessage,
                                         String role, String statusCode) {
        assertThat(deleteResourceResp).isNotNull();
        assertThat(deleteResourceResp.getRoleDeletionResponse()).isNotEmpty();
        RoleDeletionResponse roleDeletionResponse = deleteResourceResp.getRoleDeletionResponse().get(0);
        assertThat(roleDeletionResponse.getRoleName()).isEqualTo(role);
        assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo(errorMessage);
        assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(statusCode);
    }
}
