package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserIdamStatusWithEmailResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;

@Transactional
class RetrieveUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

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
    void should_retrieve_user_profile_resource_with_id() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?" + "userId=" + userProfile.getIdamId(),
                        OK,
                        UserProfileResponse.class
                );

        assertThat(retrievedResource).isNotNull();
    }

    @Test
    void should_retrieve_user_profile_resource_with_tidam_id() throws Exception {
        UserProfile user = buildUserProfile();
        user.setIdamId("1234567");
        user.setStatus(IdamStatus.ACTIVE);
        userProfileRepository.save(user);


        UserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?" + "userId=" + "1234567",
                        OK,
                        UserProfileResponse.class
                );

        assertThat(retrievedResource).isNotNull();
    }

    @Test
    void should_retrieve_user_profile_resource_with_roles_by_id() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileWithRolesResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        OK,
                        UserProfileWithRolesResponse.class
                );

        assertThat(retrievedResource).isNotNull();

        assertThat(retrievedResource.getRoles().size()).isPositive();

        Optional<UserProfile> optionalUserProfile = userProfileRepository.findByIdamId(retrievedResource.getIdamId());
        UserProfile persistedUserProfile = optionalUserProfile.get();

        List<Audit> matchedAuditRecords = getMatchedAuditRecords(auditRepository.findAll(),
                persistedUserProfile.getIdamId());
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);

        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(200);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.OK);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(retrievedResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }

    @Test
    void should_retrieve_user_profile_resource_with_roles_by_email() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileWithRolesResponse retrievedResource =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + SLASH + "roles",
                        OK,
                        UserProfileWithRolesResponse.class,
                        userProfile.getEmail()
                );

        assertThat(retrievedResource).isNotNull();
        assertThat(retrievedResource.getRoles().size()).isPositive();

        Optional<UserProfile> optionalUserProfile = userProfileRepository.findByIdamId(retrievedResource.getIdamId());
        UserProfile persistedUserProfile = optionalUserProfile.get();

        List<Audit> matchedAuditRecords = getMatchedAuditRecords(auditRepository.findAll(),
                persistedUserProfile.getIdamId());
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(200);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.OK);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(retrievedResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }

    @Test
    void should_retrieve_user_profile_resource_with_roles_by_email_fromHeader() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileWithRolesResponse retrievedResource =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + SLASH + "roles" + "?" + "email=" + "up@prdfunctestuser.com",
                        OK,
                        UserProfileWithRolesResponse.class,
                        userProfile.getEmail()
                );

        assertThat(retrievedResource).isNotNull();
        assertThat(retrievedResource.getRoles().size()).isPositive();

        Optional<UserProfile> optionalUserProfile = userProfileRepository.findByIdamId(retrievedResource.getIdamId());
        UserProfile persistedUserProfile = optionalUserProfile.get();

        List<Audit> matchedAuditRecords = getMatchedAuditRecords(auditRepository.findAll(),
                persistedUserProfile.getIdamId());
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(200);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.OK);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(retrievedResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }

    @Test
    void should_return_400_and_not_allow_get_request_on_base_url_with_no_params() throws Exception {
        MvcResult mvcResult = userProfileRequestHandlerTest.sendGet(mockMvc, APP_BASE_PATH, BAD_REQUEST);
        Assertions.assertNotNull(mvcResult);

        MockHttpServletResponse response = mvcResult.getResponse();
        Assertions.assertNotNull(response);

        assertThat(response.getStatus()).isEqualTo(400);
    }

    @Test
    void should_retrieve_user_profile_resource_with_email_from_header() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH,
                        OK,
                        UserProfileResponse.class,
                        userProfile.getEmail()
                );

        assertThat(retrievedResource).isNotNull();

    }

    @Test
    void should_not_retrieve_user_profile_resource_with_unknown_email_from_header() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + "?" + "email=" + userProfile.getEmail(),
                        NOT_FOUND,
                        UserProfileResponse.class,
                        "up@prdfunctestuser.com"
                );

        assertThat(retrievedResource).isNotNull();

    }

    @Test
    void should_return_404_when_user_profile_id_not_in_the_db() throws Exception {

        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?userId=" + UUID.randomUUID(),
                        NOT_FOUND
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    void should_return_404_when_nothing_in_the_db() throws Exception {

        userProfileRepository.delete(userProfileMap.get("user"));
        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?userId=" + UUID.randomUUID(),
                        NOT_FOUND
                );
        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    void should_return_404_when_user_profile_email_not_in_the_db() throws Exception {

        MvcResult result =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH,
                        NOT_FOUND,
                        "test@test.com"
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    void should_return_404_when_query_by_email_and_nothing_in_the_db() throws Exception {

        userProfileRepository.delete(userProfileMap.get("user"));
        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        MvcResult result =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH,
                        NOT_FOUND,
                        "test@test.com"
                );


        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    void should_return_400_when_query_by_email_header_isEmpty() throws Exception {
        MvcResult result =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH,
                        NOT_FOUND,
                        ""
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    void should_return_404_when_query_by_userId_is_empty() throws Exception {

        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?userId=",
                        NOT_FOUND
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    void should_return_badRequest_when_retrieve_userprofile_with_roles_by_no_email() throws Exception {

        MvcResult retrievedResource =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + SLASH + "roles",
                        BAD_REQUEST,
                        ""
                );

        assertThat(retrievedResource).isNotNull();

        Exception resolvedException = retrievedResource.getResolvedException();

        assertThat(resolvedException).isNotNull();
        assertThat(resolvedException.getMessage())
                .isEqualTo("No User Email provided via header");

    }


    @Test
    void should_retrieve_user_profile_IdamStatusWithEmailResponse() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");
        userProfile.setUserCategory(UserCategory.CASEWORKER);

        UserIdamStatusWithEmailResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "/idamStatus?category=caseworker",
                        OK,
                        UserIdamStatusWithEmailResponse.class
                );
        assertThat(retrievedResource).isNotNull();
        assertThat(Objects.requireNonNull(retrievedResource)
                .getUserProfiles().get(0).getEmail())
                .isNotNull();
        assertThat(Objects.requireNonNull(retrievedResource)
                .getUserProfiles().get(0).getIdamStatus())
                .isNotNull();


    }

    @Test
    void should_return_invalidRequest_when_retrieve_user_profile_IdamStatusWithEmail_with_invalid_query_param()
            throws Exception {

        MvcResult retrievedResource = userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "/idamStatus?category=casessworker",
                BAD_REQUEST
                );

        assertThat(retrievedResource).isNotNull();

        Exception resolvedException = retrievedResource.getResolvedException();

        assertThat(resolvedException).isNotNull();
        assertThat(resolvedException.getMessage())
                .isEqualTo("3 : There is a problem with your request. Please check and try again");

    }

    @Test
    void should_return_invalidRequest_when_retrieve_user_profile_IdamStatusWithEmail_() throws Exception {

        MvcResult retrievedResource = userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "/idamStatus?category=caseworker",
                NOT_FOUND
        );

        assertThat(retrievedResource).isNotNull();

        Exception resolvedException = retrievedResource.getResolvedException();

        assertThat(resolvedException).isNotNull();
        assertThat(resolvedException.getMessage())
                .isEqualTo("Could not find any profiles");

    }


}
