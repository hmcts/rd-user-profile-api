package uk.gov.hmcts.reform.userprofileapi.integration.wiremock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.patch;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static groovy.json.JsonOutput.toJson;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static uk.gov.hmcts.reform.userprofileapi.integration.SpringBootIntegrationTest.getObjectMapper;

public final class IdamWireMockStubs {

    private static final String USER_SEARCH_PATH = "/api/v1/users";
    private static final String USER_ROLES_PATH_PATTERN = "/api/v1/users/.*";
    private static final String DEFAULT_USER_ID = "ef4fac86-d3e8-47b6-88a7-c7477fb69d3f";
    private static WireMockServer idamMockServer = null;

    private IdamWireMockStubs() {
    }

    public static void registerDefaults(WireMockServer server) {
        idamMockServer = server;
        try {
            stubUserInfo(server);
            stubUserRegistration(HttpStatus.CREATED.value(), true);
            stubGetUserInfo(server);
            stubGetUserDetails(server);
            stubDeleteUser(server);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void stubDeleteUser(WireMockServer server) {
        server.stubFor(delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(204)
                        .withBody("{"
                                + "  \"response\": \"User deleted successfully.\""
                                + "}")));
    }

    private static void stubGetUserDetails(WireMockServer server) throws JsonProcessingException {
        UserInfo userDetails = UserInfo.builder()
                .givenName("Suspended")
                .familyName("User")
                .roles(List.of("pui-organisation-manager"))
                .sub("false")
                .build();

        server.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(getObjectMapper().writeValueAsString(userDetails))));
    }

    private static void stubGetUserInfo(WireMockServer server) throws JsonProcessingException {
        HashMap<String, String> data = new HashMap<>();
        data.put("active", "true");
        data.put("forename", "Super");
        data.put("surname", "User");
        data.put("email", "test@test.com");
        data.put("pending", "false");
        data.put("roles", "pui-organisation-manager");

        server.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(getObjectMapper().writeValueAsString(data))));
    }

    private static void stubUserInfo(WireMockServer server) {
        server.stubFor(
                get(urlPathEqualTo("/o/userinfo"))
                        .atPriority(10)
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                        .withBody(getUserDetailsJson())
                        )
        );
    }

    private static String getUserDetailsJson() {
        try {
            UserInfo userDetailsNew = UserInfo.builder()
                    .uid("%s")
                    .givenName("User")
                    .familyName("User")
                    .name("Super")
                    .roles(List.of("pui-organisation-manager"))
                    .sub("active")
                    .build();

            return getObjectMapper().writeValueAsString(userDetailsNew);

        } catch (Exception e) {
            throw new IllegalStateException("Unable to create IDAM userinfo response", e);
        }
    }

    public static void stubUserRegistration(int status,
                                            boolean setBodyEmpty) throws JsonProcessingException {
        String body = null;
        ErrorResponse errorResponse;
        if (status == 400 && !setBodyEmpty) {
            errorResponse = ErrorResponse.builder()
                    .status(400)
                    .errorMessage("Role to be assigned does not exist.")
                    .build();
            body = getObjectMapper().writeValueAsString(errorResponse);

        } else if (status == 409 && !setBodyEmpty) {
            errorResponse = ErrorResponse.builder()
                    .status(409)
                    .errorMessage("[A user is already registered with this email.]")
                    .build();
            body = getObjectMapper().writeValueAsString(errorResponse);
        } else if (status == 404 && !setBodyEmpty) {
            errorResponse = ErrorResponse.builder()
                    .status(404)
                    .errorMessage("16 Resource not found")
                    .errorDescription("The role to be assigned does not exist.")
                    .build();
            body = getObjectMapper().writeValueAsString(errorResponse);
        }
        idamMockServer.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/v1/users/" + UUID.randomUUID())
                        .withStatus(status)
                        .withBody(body)
                ));
    }


    public static void searchUserProfileSyncWireMock(HttpStatus status, String id) {
        boolean successful = status.is2xxSuccessful();

        int responseStatus = successful
                ? HttpStatus.OK.value()
                : status.is4xxClientError()
                ? HttpStatus.BAD_REQUEST.value()
                : status.value();

        String body = successful
                ? createUserSearchResponse(
                StringUtils.defaultIfBlank(id, DEFAULT_USER_ID))
                : null;

        idamMockServer.stubFor(
                get(urlPathEqualTo(USER_SEARCH_PATH))
                        .willReturn(
                                aResponse()
                                        .withHeader("Content-Type", "application/json")
                                        .withHeader("X-Total-Count", "1")
                                        .withBody(body)
                                        .withStatus(responseStatus)
                        )
        );
    }

    public static void stubUpdateUserFailure(
            HttpStatus status,
            boolean bodyRequired,
            String idamId) {
        String body = null;

        if (status == HttpStatus.NOT_FOUND && bodyRequired) {
            body = toJson(
                    ErrorResponse.builder()
                            .status(status.value())
                            .errorMessage("16 Resource not found")
                            .build()
            );
        }
        idamMockServer.stubFor(patch(urlMatching(USER_SEARCH_PATH + "/" + idamId))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(status.value())
                        .withBody(body)));
    }

    public static void mockWithGetFail(HttpStatus httpStatus,
                                       boolean isBodyRequired) throws JsonProcessingException {
        String body = null;
        if (httpStatus == HttpStatus.NOT_FOUND && isBodyRequired) {
            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(404)
                    .errorMessage("The user could not be found: c5d631f-af11-4816-abbe-ac6fd9b99ee9")
                    .build();
            body = getObjectMapper().writeValueAsString(errorResponse);
        }
        idamMockServer.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)
                ));

    }

    public static void stubRegistrationResponse(String id) {
        idamMockServer.stubFor(post(urlEqualTo("/api/v1/users/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withHeader("Location", "/api/v1/users/" + id)
                        .withStatus(409)
                ));
    }

    public static void mockWithUpdateRolesSuccess(String id) {
        idamMockServer.stubFor(post(urlEqualTo("/api/v1/users/" + id + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public static void mockWithUpdateRolesFailure(HttpStatus httpStatus, boolean isBodyRequired, String userId)
            throws JsonProcessingException {
        String body = null;
        ErrorResponse errorResponse;
        if (httpStatus.value() == 412 && isBodyRequired) {
            errorResponse = ErrorResponse.builder()
                    .status(412)
                    .errorMessage("One or more of the roles provided does not exist.")
                    .build();
            body = getObjectMapper().writeValueAsString(errorResponse);
        }
        idamMockServer.stubFor(post(urlEqualTo("/api/v1/users/" + userId + "/roles"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(httpStatus.value())
                        .withBody(body)));
    }

    public static void mockWithDeleteRoleSuccess() {
        idamMockServer.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                ));
    }

    public static void mockWithDeleteRoleFailure(HttpStatus httpStatus,
                                                 boolean isBodyRequired,
                                                 boolean isUnassignedRole) {
        String body = null;

        if (httpStatus == HttpStatus.PRECONDITION_FAILED && isBodyRequired) {
            String errorMessage = isUnassignedRole
                    ? "The role provided is not assigned to the user."
                    : "One or more of the roles provided does not exist.";

            body = toJson(
                    ErrorResponse.builder()
                            .status(httpStatus.value())
                            .errorMessage(errorMessage)
                            .build()
            );
        }

        ResponseDefinitionBuilder response = aResponse()
                .withStatus(httpStatus.value());

        if (body != null) {
            response
                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .withBody(body);
        }

        idamMockServer.stubFor(WireMock.delete(urlMatching("/api/v1/users/.*"))
                .willReturn(response));
    }

    public static void mockWithGetSuccess() throws JsonProcessingException {
        HashMap<Object, Object> data = new HashMap<>();
        data.put("active", "true");
        data.put("forename", "fname");
        data.put("surname", "lname");
        data.put("email", "email");
        data.put("roles", List.of("pui-case-manager"));
        idamMockServer.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(getObjectMapper().writeValueAsString(data))));

    }

    public static void mockWithGetSuccess(String id,
                                          boolean withoutStatusFields) throws JsonProcessingException {
        HashMap<String, Object> data = new HashMap<>();
        if (!withoutStatusFields) {
            data.put("active", "true");
            data.put("forename", "fname");
            data.put("surname", "lname");
            data.put("email", "test@test.com");
            data.put("roles", List.of("pui-organisation-manager", "pui-user-manager"));
        } else {
            data.put("id", id);
            data.put("active", "true");
        }

        idamMockServer.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(getObjectMapper().writeValueAsString(data))));

    }

    public static void mockWithGetSuccess(boolean withoutStatusFields) throws JsonProcessingException {
        HashMap<Object, Object> data;
        if (!withoutStatusFields) {
            data = new HashMap<>();
            data.put("active", "true");
            data.put("forename", "fname");
            data.put("surname", "lname");
            data.put("email", "test@test.com");
            data.put("roles", List.of("pui-organisation-manager", "pui-user-manager"));
        } else {
            data = new HashMap<>();
            data.put("id", "e65e5439-a8f7-4ae6-b378-cc1015b72dbb");
            data.put("active", "false");
            data.put("pending", "true");
        }

        idamMockServer.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody(getObjectMapper().writeValueAsString(data))));

    }

    public static void mockWithUpdateFail(String userId) {
        idamMockServer.stubFor(post(urlMatching(String.format("/api/v1/users/%s/roles", userId)))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                ));
    }

    private static String createUserSearchResponse(String userId) {
        return """
               [{
                 "id": "%s",
                 "forename": "Super",
                 "surname": "User",
                 "email": "dummy@email.com",
                 "active": "true",
                 "roles": [
                   "pui-case-manager"
                 ]
               }]
               """.formatted(userId);
    }

}