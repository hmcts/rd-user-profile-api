package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.client.UserProfileRequestHandlerTest.COMMON_EMAIL_PATTERN;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

@SpringBootTest(webEnvironment = MOCK)
@Transactional
class DeleteUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void should_return_204_and_delete_user_profile_resource() throws Exception {

        //user profile create and  delete
        createAndDeleteSingleUserProfile(buildCreateUserProfileData());
        verifyUserProfileDeletion(1, 2);

    }

    @Test
    void should_return_204_and_delete_multiple_user_profile_resource() throws Exception {

        UserProfileCreationResponse response1 = createUserProfile(buildCreateUserProfileData());
        UserProfileCreationResponse response2 = createUserProfile(buildCreateUserProfileData());

        //user profile two created
        List<String> userIds = new ArrayList<String>();
        userIds.add(response1.getIdamId());
        userIds.add(response2.getIdamId());
        //user profile to delete
        deleteUserProfiles(userIds, NO_CONTENT);
        verifyUserProfileDeletion(2, 4);
    }

    @Test
    void should_return_204_and_delete_user_profile_by_user_id() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();

        //user profile create and  delete
        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(mockMvc, APP_BASE_PATH,
                        data, CREATED, UserProfileCreationResponse.class);

        verifyUserProfileCreation(createdResource, CREATED, data);

        userProfileRequestHandlerTest.sendDeleteWithoutBody(mockMvc,
                APP_BASE_PATH + "/users?userId=" + createdResource.getIdamId(),
                NO_CONTENT,
                UserProfilesDeletionResponse.class);

        verifyUserProfileDeletion(1, 2);

    }

    @Test
    void should_return_204_and_delete_user_profile_by_email_pattern() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();

        //user profile create and  delete
        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(mockMvc, APP_BASE_PATH,
                        data, CREATED, UserProfileCreationResponse.class);

        verifyUserProfileCreation(createdResource, CREATED, data);

        userProfileRequestHandlerTest.sendDeleteWithoutBody(mockMvc,
                APP_BASE_PATH + "/users?emailPattern=" + COMMON_EMAIL_PATTERN,
                NO_CONTENT,
                UserProfilesDeletionResponse.class);

        List<UserProfile> userProfiles = (List<UserProfile>) userProfileRepository.findAll();
        assertThat(userProfiles.size()).isZero();
    }

    @Test
    void return401WhenNoServiceAuthHeaderProvided() throws Exception {

        UserProfileCreationResponse response1 = createUserProfile(buildCreateUserProfileData());
        //user profile two created
        List<String> userIds = new ArrayList<String>();
        userIds.add(response1.getIdamId());
        userIds.add("12345");
        //user profile to delete
        deleteUserProfilesNoServiceAuth(userIds, UNAUTHORIZED);
        List<UserProfile> userProfiles = (List<UserProfile>) userProfileRepository.findAll();
        assertThat(userProfiles.size()).isEqualTo(1);

        List<Audit> matchedAuditRecords = auditRepository.findAll();
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
    }

    @Test
    void return404WhenUnableToFindProfileForOneOfUserIdInTheDeleteRequestForMulUserProfiles() throws Exception {

        UserProfileCreationResponse response1 = createUserProfile(buildCreateUserProfileData());
        //user profile two created
        List<String> userIds = new ArrayList<String>();
        userIds.add(response1.getIdamId());
        userIds.add("12345");
        //user profile to delete
        deleteUserProfiles(userIds, NOT_FOUND);
        List<UserProfile> userProfiles = (List<UserProfile>) userProfileRepository.findAll();
        assertThat(userProfiles.size()).isEqualTo(1);

        List<Audit> matchedAuditRecords = auditRepository.findAll();
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
    }

    @Test
    void should_return_404_when_no_user_profile_resource_to_delete() throws Exception {
        List<String> userIds = new ArrayList<String>();
        userIds.add("123456");
        //user profile to delete
        deleteUserProfiles(userIds, NOT_FOUND);
    }

    @Test
    void should_return_400_when_empty_request_to_delete_user_profile_resource() throws Exception {
        List<String> userIds = new ArrayList<String>();
        //user profile to delete
        deleteUserProfiles(userIds, BAD_REQUEST);
    }

    @Test
    void should_return_400_when_emptyUserId_in_the_request_to_delete_user_profile_resource() throws Exception {
        List<String> userIds = new ArrayList<String>();
        userIds.add("123456");
        userIds.add("");
        //user profile to delete
        deleteUserProfiles(userIds, BAD_REQUEST);
    }

    private void verifyUserProfileDeletion(int expectedAuditRecords, int expectedTotalAuditRecords) {

        List<UserProfile> userProfiles = (List<UserProfile>) userProfileRepository.findAll();
        assertThat(userProfiles.size()).isZero();

        List<Audit> auditRecords = auditRepository.findAll();
        assertThat(auditRecords.size()).isEqualTo(expectedTotalAuditRecords);

        assertThat(auditRecords.stream().filter(audit ->
                audit.getIdamRegistrationResponse() == 201).collect(Collectors.toList())).hasSize(expectedAuditRecords);

        assertThat(auditRecords.stream().filter(audit ->
                audit.getIdamRegistrationResponse() == 204).collect(Collectors.toList())).hasSize(expectedAuditRecords);

        auditRecords.forEach(audit -> {
            assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
            assertThat(audit.getAuditTs()).isNotNull();
        });
    }
}
