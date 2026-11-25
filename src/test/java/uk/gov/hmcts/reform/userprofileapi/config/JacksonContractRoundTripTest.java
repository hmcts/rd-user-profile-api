package uk.gov.hmcts.reform.userprofileapi.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import uk.gov.hmcts.reform.idam.client.models.UserInfo;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UpdateUserDetails;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.IdamUserResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class JacksonContractRoundTripTest {

    private final JsonMapper jsonMapper = ApplicationJsonMapper.create();

    @Test
    void createPayloadKeepsConsentsAndDropsUnknownProperties() {
        String body = """
                {
                  "email": "person@example.com",
                  "firstName": "Ann",
                  "lastName": "Bee",
                  "languagePreference": "EN",
                  "emailCommsConsent": true,
                  "postalCommsConsent": false,
                  "userCategory": "PROFESSIONAL",
                  "userType": "EXTERNAL",
                  "roles": ["pui-case-manager"],
                  "resendInvite": true,
                  "unexpected": "ignored"
                }
                """;

        UserProfileCreationData data = jsonMapper.readValue(body, UserProfileCreationData.class);

        assertThat(data.getEmail()).isEqualTo("person@example.com");
        assertThat(data.getFirstName()).isEqualTo("Ann");
        assertThat(data.getLastName()).isEqualTo("Bee");
        assertThat(data.getLanguagePreference()).isEqualTo("EN");
        assertThat(data.isEmailCommsConsent()).isTrue();
        assertThat(data.isPostalCommsConsent()).isFalse();
        assertThat(data.getUserCategory()).isEqualTo("PROFESSIONAL");
        assertThat(data.getUserType()).isEqualTo("EXTERNAL");
        assertThat(data.getRoles()).containsExactly("pui-case-manager");
        assertThat(data.isResendInvite()).isTrue();

        JsonNode written = jsonMapper.readTree(jsonMapper.writeValueAsString(data));
        assertThat(written.get("email").asString()).isEqualTo("person@example.com");
        assertThat(written.get("emailCommsConsent").asBoolean()).isTrue();
        assertThat(written.has("unexpected")).isFalse();
    }

    @Test
    void upperCamelCaseNamesDoNotBindCreateFields() {
        String body = """
                {
                  "email": "person@example.com",
                  "FirstName": "Ann",
                  "LastName": "Bee",
                  "roles": ["pui-case-manager"]
                }
                """;

        UserProfileCreationData data = jsonMapper.readValue(body, UserProfileCreationData.class);

        assertThat(data.getEmail()).isEqualTo("person@example.com");
        assertThat(data.getFirstName()).isNull();
        assertThat(data.getLastName()).isNull();
        assertThat(data.getRoles()).containsExactly("pui-case-manager");
        assertThat(data.isEmailCommsConsent()).isFalse();
        assertThat(data.isPostalCommsConsent()).isFalse();
        assertThat(data.isResendInvite()).isFalse();
    }

    @Test
    void updatePayloadRoundTripsAndIdamPatchUsesForename() {
        String body = """
                {
                  "email": "person@example.com",
                  "firstName": "Ann",
                  "lastName": "Bee",
                  "idamStatus": "ACTIVE",
                  "rolesAdd": [{"name": "pui-finance-manager"}],
                  "rolesDelete": [{"name": "pui-case-manager"}]
                }
                """;

        UpdateUserProfileData data = jsonMapper.readValue(body, UpdateUserProfileData.class);
        assertThat(data.getFirstName()).isEqualTo("Ann");
        assertThat(data.getLastName()).isEqualTo("Bee");
        assertThat(data.getIdamStatus()).isEqualTo("ACTIVE");
        assertThat(data.getRolesAdd()).extracting(RoleName::getName).containsExactly("pui-finance-manager");
        assertThat(data.getRolesDelete()).extracting(RoleName::getName).containsExactly("pui-case-manager");

        UpdateUserDetails outbound = new UpdateUserDetails(data.getFirstName(), data.getLastName(), true);
        JsonNode written = jsonMapper.readTree(jsonMapper.writeValueAsString(outbound));
        assertThat(written.get("forename").asString()).isEqualTo("Ann");
        assertThat(written.get("surname").asString()).isEqualTo("Bee");
        assertThat(written.get("active").asBoolean()).isTrue();
        assertThat(written.has("firstName")).isFalse();
    }

    @Test
    void profileResponseUsesUserIdentifierAndKeepsNulls() {
        UserProfileResponse response = new UserProfileResponse();
        response.setIdamId("user-1");
        response.setEmail("person@example.com");
        response.setFirstName("Ann");
        response.setLastName("Bee");
        response.setIdamStatus("ACTIVE");

        String json = jsonMapper.writeValueAsString(response);
        JsonNode written = jsonMapper.readTree(json);

        assertThat(written.get("userIdentifier").asString()).isEqualTo("user-1");
        assertThat(written.has("idamId")).isFalse();
        assertThat(written.get("roles").isNull()).isTrue();

        UserProfileResponse read = jsonMapper.readValue(json, UserProfileResponse.class);
        assertThat(read.getIdamId()).isEqualTo("user-1");
        assertThat(read.getFirstName()).isEqualTo("Ann");
    }

    @Test
    void userIdsBodyBindsAsObject() {
        UserProfileDataRequest request = jsonMapper.readValue(
                "{\"userIds\":[\"one\",\"two\"]}", UserProfileDataRequest.class);

        assertThat(request.getUserIds()).containsExactly("one", "two");
        assertThat(jsonMapper.readTree(jsonMapper.writeValueAsString(request)).get("userIds").size()).isEqualTo(2);
    }

    @Test
    void idamUserAndFeedDecode() {
        IdamUserResponse user = jsonMapper.readValue("""
                {
                  "active": true,
                  "email": "person@example.com",
                  "forename": "Ann",
                  "id": "user-1",
                  "pending": false,
                  "roles": ["pui-case-manager"],
                  "surname": "Bee"
                }
                """, IdamUserResponse.class);

        assertThat(user.getId()).isEqualTo("user-1");
        assertThat(user.getForename()).isEqualTo("Ann");
        assertThat(user.getSurname()).isEqualTo("Bee");
        assertThat(user.getEmail()).isEqualTo("person@example.com");
        assertThat(user.getRoles()).containsExactly("pui-case-manager");
        assertThat(user.getActive()).isTrue();

        Set<IdamFeignClient.User> feed = jsonMapper.readValue("""
                [
                  {"id": "user-1", "email": "person@example.com", "forename": "Ann", "surname": "Bee",
                   "active": true, "roles": ["pui-case-manager"]},
                  {"id": "user-2", "email": "other@example.com", "forename": "Cy", "surname": "Dee",
                   "active": false, "roles": ["pui-finance-manager"]}
                ]
                """, jsonMapper.getTypeFactory().constructCollectionType(Set.class, IdamFeignClient.User.class));

        assertThat(feed).extracting(IdamFeignClient.User::getId).containsExactlyInAnyOrder("user-1", "user-2");
        IdamFeignClient.User first = feed.stream()
                .filter(feedUser -> "user-1".equals(feedUser.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(first.getForename()).isEqualTo("Ann");
        assertThat(first.getEmail()).isEqualTo("person@example.com");
        assertThat(first.isActive()).isTrue();
        assertThat(first.getRoles()).containsExactly("pui-case-manager");
    }

    @Test
    void idamClientUserInfoDecodesSnakeCaseNames() {
        UserInfo userInfo = jsonMapper.readValue("""
                {
                  "sub": "sub-1",
                  "uid": "uid-1",
                  "name": "Ann Bee",
                  "given_name": "Ann",
                  "family_name": "Bee",
                  "roles": ["pui-case-manager"]
                }
                """, UserInfo.class);

        assertThat(userInfo.getSub()).isEqualTo("sub-1");
        assertThat(userInfo.getUid()).isEqualTo("uid-1");
        assertThat(userInfo.getGivenName()).isEqualTo("Ann");
        assertThat(userInfo.getFamilyName()).isEqualTo("Bee");
        assertThat(userInfo.getRoles()).containsExactly("pui-case-manager");
    }

    @Test
    void registrationBodyUsesCamelCase() {
        IdamRegisterUserRequest request = new IdamRegisterUserRequest(
                "person@example.com", "Ann", "Bee", "user-1", List.of("pui-case-manager"));

        JsonNode written = jsonMapper.readTree(jsonMapper.writeValueAsString(request));
        assertThat(written.get("email").asString()).isEqualTo("person@example.com");
        assertThat(written.get("firstName").asString()).isEqualTo("Ann");
        assertThat(written.get("lastName").asString()).isEqualTo("Bee");
        assertThat(written.get("id").asString()).isEqualTo("user-1");
        assertThat(written.get("roles").get(0).asString()).isEqualTo("pui-case-manager");
    }

    @Test
    void errorResponseKeepsNames() {
        ErrorResponse error = ErrorResponse.builder()
                .errorMessage("Bad Request")
                .errorDescription("email in invalid format")
                .timeStamp("25-11-2026 10:00:00.000")
                .status(400)
                .build();

        JsonNode written = jsonMapper.readTree(jsonMapper.writeValueAsString(error));
        assertThat(written.get("errorMessage").asString()).isEqualTo("Bad Request");
        assertThat(written.get("errorDescription").asString()).isEqualTo("email in invalid format");
        assertThat(written.get("timeStamp").asString()).isEqualTo("25-11-2026 10:00:00.000");
        assertThat(written.get("status").asInt()).isEqualTo(400);
    }

    @Test
    void unauthorizedHeaderContainsErrorJson() throws Exception {
        RestAuthenticationEntryPoint entryPoint = new RestAuthenticationEntryPoint(jsonMapper);
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(
                new MockHttpServletRequest(),
                response,
                new BadCredentialsException("bad token"));

        assertThat(response.getStatus()).isEqualTo(401);
        JsonNode header = jsonMapper.readTree(response.getHeader("UnAuthorized-Token-Error"));
        assertThat(header.get("errorMessage").asString()).isEqualTo("Authentication Exception");
        assertThat(header.get("errorDescription").asString()).isEqualTo("bad token");
        assertThat(header.get("timeStamp").isNull()).isFalse();
    }
}
