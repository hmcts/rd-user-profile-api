package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;

import com.github.tomakehurst.wiremock.client.WireMock;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class AddRolesWithIdamIntTest extends AuthorizationEnabledIntegrationTest {

    String id =  UUID.randomUUID().toString();
    RoleName role1 = new RoleName("pui-case-manager");
    RoleName role2 = new RoleName("prd-Admin");

    @Before
    public void setUpWireMock() {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        idamService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/v1/users/" + id)
                        .withStatus(409)
                ));

    }

    public void mockWithGetSuccess(boolean withoutStatusFields) {

        String body;
        if (!withoutStatusFields) {

            body = "{"
                    + "  \"active\": \"true\","
                    + "  \"forename\": \"fname\","
                    + "  \"surname\": \"lname\","
                    + "  \"email\": \"user@hmcts.net\","
                    + "  \"roles\": ["
                    + "    \"pui-organisation-manager\","
                    + "    \"pui-user-manager\""
                    + "  ]"
                    + "}";
        } else {
            body = "{"
                    + "  \"id\": \" " + id + "\","
                    + "  \"active\": \"true\""
                    + "}";
        }

        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(body)));

    }

    public void mockWithUpdateSuccess() {
        idamService.stubFor(put(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public void mockWithUpdateRolesSuccess() {
        idamService.stubFor(post(urlEqualTo("/api/v1/users/" + id + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }


    public void mockWithUpdateRolesFailure(HttpStatus httpStatus, boolean isBodyRequired, String userId) {
        String body = null;
        if (httpStatus.value() == 412 && isBodyRequired) {
            body = "{"
                    + "\"status\": \"412\","
                    + "\"errorMessage\": \"One or more of the roles provided does not exist.\""
                    + "}";
        }
        idamService.stubFor(post(urlEqualTo("/api/v1/users/" + userId + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));
    }

    public void mockWithDeleteRoleSuccess() {
        idamService.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public void mockWithDeleteRoleFailure(HttpStatus httpStatus, boolean isBodyRequired) {
        String body = null;
        if (httpStatus.value() == 412 && isBodyRequired) {
            body = "{"
                    + "\"status\": \"412\","
                    + "\"errorMessage\": \"One or more of the roles provided does not exist.\""
                    + "}";
        }
        idamService.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));
    }


    @Test
    public void should_return_200_and_add_roles_to_user_profile_resource() throws Exception {

        mockWithGetSuccess(true);
        mockWithUpdateSuccess();
        mockWithUpdateRolesSuccess();
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );


        String userId = createdResource.getIdamId();
        assertThat(userId).isNotNull();

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesAdd(roles);
        userProfileRequestHandlerTest.sendPut(
                        mockMvc,
                  APP_BASE_PATH + "/" + userId,
                        userRoles,
                        OK
        );

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body() throws Exception {
        UpdateUserProfileData userRoles = new UpdateUserProfileData();
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        String userId = " ";
        userRoles.setRolesAdd(roles);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                BAD_REQUEST
        );

    }

    @Test
    public void should_return_200_and_add_delete_roles_to_user_profile_resource() throws Exception {

        mockWithGetSuccess(true);
        mockWithUpdateSuccess();
        mockWithUpdateRolesSuccess();
        mockWithDeleteRoleSuccess();
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );


        String userId = createdResource.getIdamId();
        assertThat(userId).isNotNull();

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesAdd(roles);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                OK
        );

        UpdateUserProfileData userRoles1 = new UpdateUserProfileData();
        RoleName role = new RoleName("pui-case-manager");
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(role);
        userRoles1.setRolesDelete(rolesDelete);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles1,
                OK
        );

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body_delete_roles() throws Exception {
        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        String userId = " ";
        userRoles.setRolesDelete(roles);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                BAD_REQUEST
        );

    }


    @Test
    public void should_see_error_message_from_idam_when_role_addition_fails_and_sidam_returns_error_response()
            throws Exception {

        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithUpdateRolesFailure(HttpStatus.PRECONDITION_FAILED, true, userId);

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesAdd(roles);
        UserProfileRolesResponse userProfileRolesResponse = userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                OK,
                UserProfileRolesResponse.class
        );

        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("412");
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage())
                .isEqualTo("One or more of the roles provided does not exist.");

    }

    @Test
    public void sld_see_err_msg_from_idam_when_role_addition_fails_and_sidam_does_not_returns_error_response()
            throws Exception {

        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithUpdateRolesFailure(HttpStatus.PRECONDITION_FAILED, false, userId);

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesAdd(roles);
        UserProfileRolesResponse userProfileRolesResponse = userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                OK,
                UserProfileRolesResponse.class
        );

        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode()).isEqualTo("412");
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage())
                .isEqualTo("Problem while role addition/deletion");

    }

    @Test
    public void should_see_error_message_from_idam_when_role_deletion_fails_and_sidam_returns_error_response()
            throws Exception {

        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithDeleteRoleFailure(HttpStatus.PRECONDITION_FAILED, true);

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesDelete(roles);
        UserProfileRolesResponse userProfileRolesResponse = userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                OK,
                UserProfileRolesResponse.class
        );

        userProfileRolesResponse.getRoleDeletionResponse().forEach(roleDeletionResponse -> {
            assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo("412");
            assertThat(roleDeletionResponse.getIdamMessage())
                    .isEqualTo("One or more of the roles provided does not exist.");
        });

    }

    @Test
    public void should_see_error_message_from_idam_when_role_deletion_fails_and_sidam_does_not_returns_error_response()
            throws Exception {

        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithDeleteRoleFailure(HttpStatus.PRECONDITION_FAILED, false);

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesDelete(roles);
        UserProfileRolesResponse userProfileRolesResponse = userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                OK,
                UserProfileRolesResponse.class
        );

        userProfileRolesResponse.getRoleDeletionResponse().forEach(roleDeletionResponse -> {
            assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo("412");
            assertThat(roleDeletionResponse.getIdamMessage()).isEqualTo("Problem while role addition/deletion");
        });

    }

}
