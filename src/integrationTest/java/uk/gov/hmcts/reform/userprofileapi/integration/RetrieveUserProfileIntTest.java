package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class RetrieveUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        UserProfile user1 = buildUserProfile();
        user1.setStatus(IdamStatus.ACTIVE);
        user1 = userProfileRepository.save(user1);


        assertTrue(userProfileRepository.existsById(user1.getId()));

        userProfileMap = new HashMap<>();
        userProfileMap.put("user", user1);

    }

    @Test
    public void should_retrieve_user_profile_resource_with_id() throws Exception {
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
    public void should_retrieve_user_profile_resource_with_tidam_id() throws Exception {
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
    public void should_retrieve_user_profile_resource_with_roles_by_id() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileWithRolesResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + userProfile.getIdamId() + "/roles",
                        OK,
                        UserProfileWithRolesResponse.class
                );

        assertThat(retrievedResource).isNotNull();

        assertThat(retrievedResource.getRoles().size()).isGreaterThan(0);

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
    public void should_retrieve_user_profile_resource_with_roles_by_email() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileWithRolesResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + SLASH + "roles" + "?" + "email=" + userProfile.getEmail(),
                        OK,
                        UserProfileWithRolesResponse.class
                );

        assertThat(retrievedResource).isNotNull();
        assertThat(retrievedResource.getRoles().size()).isGreaterThan(0);

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
    public void should_retrieve_user_profile_resource_with_roles_by_email_fromHeader() throws Exception {
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
        assertThat(retrievedResource.getRoles().size()).isGreaterThan(0);

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
    public void should_retrieve_user_profile_resource_with_email() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?" + "email=" + userProfile.getEmail(),
                        OK,
                        UserProfileResponse.class
                );

        assertThat(retrievedResource).isNotNull();

    }

    @Test
    public void should_retrieve_user_profile_resource_with_email_from_header() throws Exception {
        UserProfile userProfile = userProfileMap.get("user");

        UserProfileResponse retrievedResource =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + "?" + "email=" + userProfile.getEmail(),
                        OK,
                        UserProfileResponse.class,
                        userProfile.getEmail()
                );

        assertThat(retrievedResource).isNotNull();

    }

    @Test
    public void should_not_retrieve_user_profile_resource_with_unknown_email_from_header() throws Exception {
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
    public void should_return_404_when_user_profile_id_not_in_the_db() throws Exception {

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?userId=" + UUID.randomUUID().toString(),
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_404_when_nothing_in_the_db() throws Exception {

        userProfileRepository.delete(userProfileMap.get("user"));
        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?userId=" + UUID.randomUUID().toString(),
                NOT_FOUND
            );
        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_404_when_user_profile_email_not_in_the_db() throws Exception {

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=" + "randomemail@somewhere.com",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_404_when_query_by_email_and_nothing_in_the_db() throws Exception {

        userProfileRepository.delete(userProfileMap.get("user"));
        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=" + "randomemail@somewhere.com",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_404_when_query_by_email_is_empty() throws Exception {
        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_404_when_query_by_email_is_empty_and_header_isEmpty() throws Exception {
        MvcResult result =
                userProfileRequestHandlerTest.sendGetFromHeader(
                        mockMvc,
                        APP_BASE_PATH + "?email=",
                        NOT_FOUND,
                        ""
                );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_404_when_query_by_userId_is_empty() throws Exception {

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?userId=",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }
}
