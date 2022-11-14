package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;

@Transactional
class RetrieveUserProfileWithIdamErrorsIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;

    @BeforeEach
    public void setUpWireMock() {

        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        setSidamRegistrationMockWithStatus(HttpStatus.CREATED.value(), true);
        mockWithGetFail(NOT_FOUND, false);

        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        UserProfile user1 = buildUserProfile();
        user1.setStatus(IdamStatus.ACTIVE);
        user1 = userProfileRepository.save(user1);

        Assertions.assertTrue(userProfileRepository.existsById(user1.getId()));

        userProfileMap = new HashMap<>();
        userProfileMap.put("user", user1);
    }

    @Test
    void should_fail_when_idam_returns_unsuccessfull_response_with_roles_by_id() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        NOT_FOUND
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

        List<Audit> matchedAuditRecords = getMatchedAuditRecords(auditRepository.findAll(), userProfile.getIdamId());
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(404);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.NOT_FOUND);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isNotNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

    @Test
    void shouldFailWhenIdamReturnsUnSuccessfullResponseResourceWithRolesByEmailFromHeader() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");
        MvcResult result =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + SLASH + "roles" + "?" + "email=" + userProfile.getEmail(),
                        NOT_FOUND,
                        userProfile.getEmail()
                );

        List<Audit> matchedAuditRecords = getMatchedAuditRecords(auditRepository.findAll(), userProfile.getIdamId());
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(404);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.NOT_FOUND);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isNotNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

    @Test
    void should_see_idam_error_message_when_idam_returns_404_response_with_roles_by_id() throws Exception {

        mockWithGetFail(NOT_FOUND, true);
        UserProfile userProfile = userProfileMap.get("user");

        ErrorResponse errorResponse =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        NOT_FOUND,
                        ErrorResponse.class
                );

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrorMessage()).isEqualTo("16 Resource not found");
        assertThat(errorResponse.getErrorDescription())
                .isEqualTo("The user could not be found: c5d631f-af11-4816-abbe-ac6fd9b99ee9");
    }

    @Test
    void should_see_idam_error_message_when_idam_returns_404_and_does_not_send_response_with_roles_by_id()
            throws Exception {

        UserProfile userProfile = userProfileMap.get("user");

        ErrorResponse errorResponse =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        NOT_FOUND,
                        ErrorResponse.class
                );

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrorMessage()).isEqualTo("16 Resource not found");
        assertThat(errorResponse.getErrorDescription()).isEqualTo("16 Resource not found");
    }

    @Test
    void should_return_401_when_idam_server_throws_500_error_getUserProfileWithRolesById() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        setSidamRegistrationMockWithStatus(INTERNAL_SERVER_ERROR.value(), true);
        mockWithGetFail(INTERNAL_SERVER_ERROR, false);

        ErrorResponse errorResponse =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        UNAUTHORIZED,
                        ErrorResponse.class
                );

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrorDescription()).isEqualTo(IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE);
    }

    @Test
    void should_return_401_when_idam_server_throws_500_error_getUserProfileWithRolesByEmail() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        setSidamRegistrationMockWithStatus(INTERNAL_SERVER_ERROR.value(), true);
        mockWithGetFail(INTERNAL_SERVER_ERROR, false);

        ErrorResponse errorResponse =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + SLASH +  "roles",
                        UNAUTHORIZED,
                        ErrorResponse.class,
                        userProfile.getEmail()
                );

        assertThat(errorResponse).isNotNull();
        assertThat(errorResponse.getErrorDescription()).isEqualTo(IdamStatusResolver.IDAM_5XX_ERROR_RESPONSE);
    }

}
