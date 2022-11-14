package uk.gov.hmcts.reform.userprofileapi.integration;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;


@Transactional
class AddAndDeleteRolesWithIdamIntTest extends AuthorizationEnabledIntegrationTest {

    String id = UUID.randomUUID().toString();
    RoleName role1 = new RoleName("pui-case-manager");
    RoleName role2 = new RoleName("prd-Admin");

    @BeforeEach
    public void setUpWireMock() {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        idamMockService.stubFor(post(urlEqualTo("/api/v1/users/registration"))
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
                    + "  \"email\": \"test@test.com\","
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

        idamMockService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(body)));

    }

    public void mockWithUpdateSuccess() {
        idamMockService.stubFor(put(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public void mockWithUpdateRolesSuccess() {
        idamMockService.stubFor(post(urlEqualTo("/api/v1/users/" + id + "/roles"))
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
        idamMockService.stubFor(post(urlEqualTo("/api/v1/users/" + userId + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));
    }

    public void mockWithDeleteRoleSuccess() {
        idamMockService.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public void mockWithDeleteRoleFailure(HttpStatus httpStatus, boolean isBodyRequired, boolean isUnassignedRole) {
        String body = null;
        if (httpStatus.value() == 412 && isBodyRequired && !isUnassignedRole) {
            body = "{"
                    + "\"status\": \"412\","
                    + "\"errorMessage\": \"One or more of the roles provided does not exist.\""
                    + "}";
        }
        if (httpStatus.value() == 412 && isBodyRequired && isUnassignedRole) {
            body = "{"
                    + "\"status\": \"412\","
                    + "\"errorMessage\": \"The role provided is not assigned to the user.\""
                    + "}";
        }
        idamMockService.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));
    }

    @Test
    void shouldReturn200AndAddRolesToUserProfileResource() throws Exception {

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
    void shouldReturn400AndNotCreateUserProfileWhenEmptyBody() throws Exception {
        UpdateUserProfileData userRoles = new UpdateUserProfileData();
        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        String userId = " ";
        userRoles.setRolesAdd(roles);
        MvcResult mvcResult =
                userProfileRequestHandlerTest.sendPut(
                        mockMvc,
                        APP_BASE_PATH + "/" + userId,
                        userRoles,
                        BAD_REQUEST
                );
        final MockHttpServletResponse response = mvcResult.getResponse();
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(400);

    }

    @Test
    void shouldReturn200AndAddDeleteRolesToUserProfileResource() throws Exception {

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
    void shouldReturn400AndNotCreateUserProfileWhenEmptyBodyDeleteRoles() throws Exception {
        UpdateUserProfileData userRoles = new UpdateUserProfileData();

        Set<RoleName> roles = new HashSet<>();
        roles.add(role1);
        roles.add(role2);
        String userId = " ";
        userRoles.setRolesDelete(roles);


        MockHttpServletResponse response1 =
                userProfileRequestHandlerTest.sendPut(
                        mockMvc,
                        APP_BASE_PATH + "/" + userId,
                        userRoles,
                        BAD_REQUEST
                ).getResponse();
        assertThat(response1).isNotNull();
        assertThat(response1.getStatus()).isEqualTo(400);

        final MockHttpServletResponse response2 = userProfileRequestHandlerTest.sendPut(
                mockMvc,
                APP_BASE_PATH + "/" + userId,
                userRoles,
                BAD_REQUEST
        ).getResponse();

        assertThat(response2).isNotNull();
        assertThat(response2.getStatus()).isEqualTo(400);
    }


    @ParameterizedTest
    @MethodSource("updateFailScenarioArguments")
    void should_see_error_message_from_idam_when_role_addition_fails(boolean isBodyRequired,
                                                                     String expectedErrorMessage) throws Exception {

        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithUpdateRolesFailure(HttpStatus.PRECONDITION_FAILED, isBodyRequired, userId);

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
                .isEqualTo(expectedErrorMessage);

    }

    @ParameterizedTest
    @MethodSource("deleteFailScenarioArguments")
    void shouldGetErrorMessageFromIdamWhenRoleDeletionFails(final boolean isBodyRequired,
                                                            final boolean isUnassignedRole,
                                                            final String statusCode,
                                                            final String expectedErrorMessage) throws Exception {

        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithDeleteRoleFailure(HttpStatus.PRECONDITION_FAILED, isBodyRequired, isUnassignedRole);

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
            assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(statusCode);
            assertThat(roleDeletionResponse.getIdamMessage())
                    .isEqualTo(expectedErrorMessage);
        });

    }


    @Test
    void shouldGetErrorMessageFromIdamWhenRoleDeletionFails5xx() throws Exception {
        final boolean isBodyRequired = false;
        final boolean isUnassignedRole = false;
        final String statusCode = "500";
        final String expectedErrorMessage = IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE;
        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithDeleteRoleFailure(HttpStatus.INTERNAL_SERVER_ERROR, isBodyRequired, isUnassignedRole);

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
        String expectedStatusCode = "401";
        userProfileRolesResponse.getRoleDeletionResponse().forEach(roleDeletionResponse -> {
            assertThat(roleDeletionResponse.getIdamStatusCode()).isEqualTo(expectedStatusCode);
            assertThat(roleDeletionResponse.getIdamMessage())
                    .isEqualTo(expectedErrorMessage);
        });

    }

    @Test
    void should_see_error_message_from_idam_when_role_addition_fails_5xx() throws Exception {
        final boolean isBodyRequired = false;
        final String statusCode = "500";
        final String expectedErrorMessage = IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE;
        UserProfile userProfile = buildUserProfile();
        userProfile.setStatus(IdamStatus.ACTIVE);
        UserProfile persistedUserProfile = userProfileRepository.save(userProfile);

        String userId = persistedUserProfile.getIdamId();
        assertThat(userId).isNotNull();

        mockWithUpdateRolesFailure(HttpStatus.INTERNAL_SERVER_ERROR, isBodyRequired, userId);

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
        String expectedStatusCode = "401";
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamStatusCode())
                .isEqualTo(expectedStatusCode);
        assertThat(userProfileRolesResponse.getRoleAdditionResponse().getIdamMessage())
                .isEqualTo(expectedErrorMessage);

    }


    private static Stream<Arguments> updateFailScenarioArguments() {
        return Stream.of(arguments(
                true, "One or more of the roles provided does not exist.",
                false, "Problem while role addition/deletion"
        ));
    }

    private static Stream<Arguments> deleteFailScenarioArguments() {
        return Stream.of(arguments(
                true, false, "412", "One or more of the roles provided does not exist.",
                true, true, "412", "The role provided is not assigned to the user.",
                false, false, "412", "Problem while role addition/deletion"
        ));
    }

}
