package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfile;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfileWithDeletedStatus;
import static uk.gov.hmcts.reform.userprofileapi.helper.UserProfileTestDataBuilder.buildUserProfileWithSuspendedStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class RetrieveMultipleUserProfilesIntTest extends AuthorizationEnabledIntegrationTest {

    private Map<String, UserProfile> userProfileMap;
    private Map<String, UserProfile> userProfileMapWithUuid;
    private List<String> userIds;
    private List<String> suspendedUserId;

    @Autowired
    private WebApplicationContext webApplicationContext;

    public void mockWithGetSuccess() {
        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(200)
                        .withBody("{"
                                + "  \"active\": \"true\","
                                + "  \"forename\": \"fname\","
                                + "  \"surname\": \"lname\","
                                + "  \"email\": \"user@hmcts.net\","
                                + "  \"roles\": ["
                                + "    \"pui-case-manager\""
                                + "  ]"
                                + "}")));

    }

    public void mockWithGetFail() {
        idamService.stubFor(get(urlMatching("/api/v1/users/.*"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(404)
                ));

    }

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        userProfileRepository.deleteAll();
        testAuditRepository.deleteAll();

        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        //adding 2 userprofiles with PENDING and 2 with DELETED status
        UserProfile user1 = buildUserProfile();
        user1.setStatus(IdamStatus.ACTIVE);
        user1 = userProfileRepository.save(user1);

        UserProfile user2 = buildUserProfile();
        user2.setStatus(IdamStatus.ACTIVE);
        user2 = userProfileRepository.save(user2);

        userProfileMap = new HashMap<>();
        userProfileMap.put("user1", user1);
        userProfileMap.put("user2", user2);

        UserProfile user3 = userProfileRepository.save(buildUserProfileWithDeletedStatus());
        UserProfile user4 = userProfileRepository.save(buildUserProfileWithDeletedStatus());
        UserProfile user5 = userProfileRepository.save(buildUserProfileWithSuspendedStatus());
        
        userProfileMap.put("user3", user3);
        userProfileMap.put("user4", user4);
        userProfileMap.put("user5", user5);

        userProfileMapWithUuid = new HashMap<>();
        userProfileMapWithUuid.put(user1.getIdamId(), user1);
        userProfileMapWithUuid.put(user2.getIdamId(), user2);
        userProfileMapWithUuid.put(user3.getIdamId(), user3);
        userProfileMapWithUuid.put(user4.getIdamId(), user4);
        userProfileMapWithUuid.put(user5.getIdamId(), user5);

        userIds = new ArrayList<String>();
        userIds.add(user1.getIdamId());
        userIds.add(user2.getIdamId());
        userIds.add(user3.getIdamId());
        userIds.add(user4.getIdamId());
        userIds.add(user5.getIdamId());

        suspendedUserId = new ArrayList<>();
        suspendedUserId.add(user5.getIdamId());
    }

    @Test
    public void should_retrieve_multiple_user_profiles_with_showDeleted_true() throws Exception {

        mockWithGetSuccess();
        UserProfileDataRequest request = new UserProfileDataRequest(userIds);
        request.getUserIds().add(UUID.randomUUID().toString());

        UserProfileDataResponse response = getMultipleUsers(request, OK,"true","true");

        assertThat(response).isNotNull();
        assertThat(response.getUserProfiles().size()).isEqualTo(5);

        response.getUserProfiles().forEach(getUserProfilesResponse -> {
            UserProfile up =  userProfileMapWithUuid.get(getUserProfilesResponse.getIdamId());
            assertThat(getUserProfilesResponse.getEmail()).isEqualTo(up.getEmail());
            assertThat(getUserProfilesResponse.getFirstName()).isEqualTo(up.getFirstName());
            assertThat(getUserProfilesResponse.getLastName()).isEqualTo(up.getLastName());
            assertThat(getUserProfilesResponse.getIdamStatus()).isEqualTo(up.getStatus().name());
            if (getUserProfilesResponse.getIdamStatus().equals(IdamStatus.ACTIVE.name())) {
                assertThat(getUserProfilesResponse.getRoles().size()).isEqualTo(1);
                assertThat(getUserProfilesResponse.getRoles().get(0)).isEqualTo("pui-case-manager");
            }
        });

        Audit audit1 = testAuditRepository.findByUserProfile(userProfileMap.get("user1")).orElse(null);
        assertThat(audit1).isNotNull();
        assertThat(audit1.getIdamRegistrationResponse()).isEqualTo(200);

        Audit audit2 = testAuditRepository.findByUserProfile(userProfileMap.get("user2")).orElse(null);
        assertThat(audit2).isNotNull();
        assertThat(audit2.getIdamRegistrationResponse()).isEqualTo(200);

        Audit audit3 = testAuditRepository.findByUserProfile(userProfileMap.get("user3")).orElse(null);
        assertThat(audit3).isNull();

        Audit audit4 = testAuditRepository.findByUserProfile(userProfileMap.get("user4")).orElse(null);
        assertThat(audit4).isNull();
    }

    @Test
    public void should_retrieve_multiple_user_profiles_with_showDeleted_false() throws Exception {

        UserProfileDataRequest request = new UserProfileDataRequest(userIds);

        UserProfileDataResponse response = getMultipleUsers(request, OK,"false", "true");

        assertThat(response).isNotNull();
        assertThat(response.getUserProfiles().size()).isEqualTo(3);
    }

    @Test
    public void should_retrieve_a_suspended_user_profiles_with_showDeleted_false() throws Exception {
        UserProfileDataRequest request = new UserProfileDataRequest(suspendedUserId);

        UserProfileDataResponse response = getMultipleUsers(request, OK,"false", "true");

        assertThat(response).isNotNull();
        assertThat(response.getUserProfiles().size()).isEqualTo(1);
        assertThat(response.getUserProfiles().get(0).getIdamStatus()).isEqualTo(IdamStatus.SUSPENDED.name());
    }

    @Test
    public void should_retrieve_multiple_user_profiles_with_idam_failure() throws Exception {

        mockWithGetFail();
        UserProfileDataRequest request = new UserProfileDataRequest(userIds);
        request.getUserIds().add(UUID.randomUUID().toString());

        UserProfileDataResponse response = getMultipleUsers(request, OK, "true", "true");

        assertThat(response).isNotNull();
        assertThat(response.getUserProfiles().size()).isEqualTo(5);

        response.getUserProfiles().forEach(getUserProfilesResponse -> {
            UserProfile up =  userProfileMapWithUuid.get(getUserProfilesResponse.getIdamId());
            assertThat(getUserProfilesResponse.getEmail()).isEqualTo(up.getEmail());
            assertThat(getUserProfilesResponse.getFirstName()).isEqualTo(up.getFirstName());
            assertThat(getUserProfilesResponse.getLastName()).isEqualTo(up.getLastName());
            assertThat(getUserProfilesResponse.getIdamStatus()).isEqualTo(up.getStatus().name());
            assertThat(getUserProfilesResponse.getRoles()).isNull();
            assertThat(getUserProfilesResponse.getIdamMessage()).isNotEmpty();
            if (IdamStatus.ACTIVE == up.getStatus()) {
                assertThat(getUserProfilesResponse.getIdamStatusCode()).isEqualTo("404");
            } else {
                assertThat(getUserProfilesResponse.getIdamStatusCode()).isEqualTo(" ");
            }
        });

        Audit audit1 = testAuditRepository.findByUserProfile(userProfileMap.get("user1")).orElse(null);
        assertThat(audit1).isNotNull();
        assertThat(audit1.getIdamRegistrationResponse()).isEqualTo(404);

        Audit audit2 = testAuditRepository.findByUserProfile(userProfileMap.get("user2")).orElse(null);
        assertThat(audit2).isNotNull();
        assertThat(audit2.getIdamRegistrationResponse()).isEqualTo(404);

        Audit audit3 = testAuditRepository.findByUserProfile(userProfileMap.get("user3")).orElse(null);
        assertThat(audit3).isNull();

        Audit audit4 = testAuditRepository.findByUserProfile(userProfileMap.get("user4")).orElse(null);
        assertThat(audit4).isNull();
    }

    @Test
    public void should_return_400_multiple_user_profiles_with_invalid_param() throws Exception {

        UserProfileDataRequest request = new UserProfileDataRequest(userIds);
        getMultipleUsers(request, HttpStatus.BAD_REQUEST, "invalid", "true");
    }

    @Test
    public void should_return_400_multiple_user_profiles_with_no_user_ids_in_request() throws Exception {

        UserProfileDataRequest request = new UserProfileDataRequest(new ArrayList<String>());
        getMultipleUsers(request, HttpStatus.BAD_REQUEST, "true", "true");
    }

    @Test
    public void should_return_404_multiple_user_profiles_with_user_ids_not_in_db() throws Exception {

        List<String> userIdList = new ArrayList<String>();
        userIdList.add(UUID.randomUUID().toString());
        userIdList.add(UUID.randomUUID().toString());
        UserProfileDataRequest request = new UserProfileDataRequest(new ArrayList<String>());

        getMultipleUsers(request, HttpStatus.BAD_REQUEST, "true", "true");
    }

    @Test
    public void should_retrieve_multiple_user_profiles_without_roles() throws Exception {

        mockWithGetSuccess();
        UserProfileDataRequest request = new UserProfileDataRequest(userIds);
        request.getUserIds().add(UUID.randomUUID().toString());

        UserProfileDataResponse response = getMultipleUsers(request, OK,"true", "false");

        assertThat(response).isNotNull();
        assertThat(response.getUserProfiles().size()).isEqualTo(5);

        response.getUserProfiles().forEach(getUserProfilesResponse -> {
            UserProfile up =  userProfileMapWithUuid.get(getUserProfilesResponse.getIdamId());
            assertThat(getUserProfilesResponse.getEmail()).isEqualTo(up.getEmail());
            assertThat(getUserProfilesResponse.getFirstName()).isEqualTo(up.getFirstName());
            assertThat(getUserProfilesResponse.getLastName()).isEqualTo(up.getLastName());
            assertThat(getUserProfilesResponse.getIdamStatus()).isEqualTo(up.getStatus().name());
            assertThat(getUserProfilesResponse.getRoles()).isNull();
            assertThat(getUserProfilesResponse.getIdamStatusCode()).isEqualTo(" ");
            assertThat(getUserProfilesResponse.getIdamMessage()).isEqualTo(IdamStatusResolver.NO_IDAM_CALL);
        });
    }

}
