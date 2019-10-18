package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.*;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class AddRolesWithIdamIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;
    String id =  UUID.randomUUID().toString();
    private MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext webApplicationContext;

    @Autowired
    protected ObjectMapper objectMapper;

    @Rule
    public WireMockRule idamService = new WireMockRule(5000);

    @Before
    public void setUpWireMock() {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        idamService.stubFor(WireMock.post(urlEqualTo("/api/v1/users/registration"))
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
        idamService.stubFor(WireMock.post(urlEqualTo("/api/v1/users/" + id + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }


    public void mockWithUpdateRolesFailure() {
        idamService.stubFor(WireMock.post(urlEqualTo("/api/v1/users/" + id + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                ));
    }

    public void mockWithDeleteRoleSuccess() {
        idamService.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }


    @Test
    public void should_return_200_and_add_roles_to_user_profile_resource() throws Exception {

        mockWithGetSuccess(true);
        mockWithUpdateSuccess();
        mockWithUpdateRolesSuccess();
        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        HttpStatus.CREATED,
                        CreateUserProfileResponse.class
                );


        String userId = createdResource.getIdamId();
        assertThat(userId).isNotNull();

        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        RoleName role1 = new RoleName("pui-case-manager");
        RoleName role2 = new RoleName("prd-Admin");

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesAdd(roles);
        userProfileRequestHandlerTest.sendPut(
                        mockMvc,
                  APP_BASE_PATH + "/" + userId + "?origin=exui",
                        userRoles,
                        HttpStatus.OK
        );

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body() throws Exception {
        UpdateUserProfileData userRoles = new UpdateUserProfileData();
        RoleName role1 = new RoleName("pui-case-manager");
        RoleName role2 = new RoleName("prd-Admin");

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        String userId = " ";
        userRoles.setRolesAdd(roles);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId + "?origin=exui",
                userRoles,
                HttpStatus.BAD_REQUEST
        );

    }

    @Test
    public void should_return_200_and_add_delete_roles_to_user_profile_resource() throws Exception {

        mockWithGetSuccess(true);
        mockWithUpdateSuccess();
        mockWithUpdateRolesSuccess();
        mockWithDeleteRoleSuccess();
        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        HttpStatus.CREATED,
                        CreateUserProfileResponse.class
                );


        String userId = createdResource.getIdamId();
        assertThat(userId).isNotNull();

        UpdateUserProfileData userRoles = new UpdateUserProfileData();
        RoleName role1 = new RoleName("pui-case-manager");
        RoleName role2 = new RoleName("prd-Admin");

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);

        userRoles.setRolesAdd(roles);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId + "?origin=exui",
                userRoles,
                HttpStatus.OK
        );

        UpdateUserProfileData userRoles1 = new UpdateUserProfileData();
        RoleName role = new RoleName("pui-case-manager");
        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(role);
        userRoles1.setRolesDelete(rolesDelete);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId + "?origin=exui",
                userRoles1,
                HttpStatus.OK
        );

    }

    @Test
    public void should_return_200_and_update_user_attributes_to_user_profile_resource() throws Exception {

        mockWithGetSuccess(true);
        mockWithUpdateSuccess();
        mockWithUpdateRolesSuccess();
        mockWithDeleteRoleSuccess();
        CreateUserProfileData data = buildCreateUserProfileData();

        CreateUserProfileResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        HttpStatus.CREATED,
                        CreateUserProfileResponse.class
                );


        String userId = createdResource.getIdamId();
        assertThat(userId).isNotNull();


        UpdateUserProfileData userRoles = new UpdateUserProfileData();
        userRoles.setFirstName("firstName");
        userRoles.setLastName("LastName");
        userRoles.setEmail("kpr@gmail.com");
        userRoles.setIdamStatus(IdamStatus.SUSPENDED.name());
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId + "?origin=exui",
                userRoles,
                HttpStatus.OK
        );

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body_delete_roles() throws Exception {
        UpdateUserProfileData userRoles = new UpdateUserProfileData();
        RoleName role1 = new RoleName("pui-case-manager");
        RoleName role2 = new RoleName("prd-Admin");

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        String userId = " ";
        userRoles.setRolesDelete(roles);
        userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId + "?orign=exui",
                userRoles,
                HttpStatus.BAD_REQUEST
        );

    }

}
