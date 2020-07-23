package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.ArrayList;
import java.util.List;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class DeleteUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_return_204_and_delete_user_profile_resource() throws Exception {

        //user profile create and  delete
        createAndDeleteSingleUserProfile(buildCreateUserProfileData());
        verifyUserProfileDeletion();

    }

    @Test
    public void should_return_204_and_delete_multiple_user_profile_resource() throws Exception {

        UserProfileCreationResponse response1 = createUserProfile(buildCreateUserProfileData());
        UserProfileCreationResponse response2 = createUserProfile(buildCreateUserProfileData());

        //user profile two created
        List<String> userIds = new ArrayList<String>();
        userIds.add(response1.getIdamId());
        userIds.add(response2.getIdamId());
        //user profile to delete
        deleteUserProfiles(userIds, NO_CONTENT);
        verifyUserProfileDeletion();
    }

    @Test
    public void should_return_404_when_unable_to_find_profile_for_one_of_user_id_in_the_delete_request_for_multiple_user_profiles() throws Exception {

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
    public void should_return_404_when_no_user_profile_resource_to_delete() throws Exception {
        List<String> userIds = new ArrayList<String>();
        userIds.add("123456");
        //user profile to delete
        deleteUserProfiles(userIds, NOT_FOUND);
    }

    @Test
    public void should_return_400_when_empty_request_to_delete_user_profile_resource() throws Exception {
        List<String> userIds = new ArrayList<String>();
        //user profile to delete
        deleteUserProfiles(userIds, BAD_REQUEST);
    }

    @Test
    public void should_return_400_when_emptyUserId_in_the_request_to_delete_user_profile_resource() throws Exception {
        List<String> userIds = new ArrayList<String>();
        userIds.add("123456");
        userIds.add("");
        //user profile to delete
        deleteUserProfiles(userIds, BAD_REQUEST);
    }

    private void verifyUserProfileDeletion() {

        List<UserProfile> userProfiles = (List<UserProfile>) userProfileRepository.findAll();
        assertThat(userProfiles.size()).isEqualTo(0);

        List<Audit> matchedAuditRecords = auditRepository.findAll();
        assertThat(matchedAuditRecords.size()).isEqualTo(1);
        Audit audit = matchedAuditRecords.get(0);
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(204);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getAuditTs()).isNotNull();
    }
}
