package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.TestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.integration.util.TestUserProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class RetrieveUserProfileIntTest {

    private static final String APP_BASE_PATH = "/profiles";
    private static final String SLASH = "/";

    @Autowired
    private TestUserProfileRepository testUserProfileRepository;

    @Autowired
    private TestRequestHandler testRequestHandler;

    private Map<String, UserProfile> userProfileMap;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Iterable<UserProfile> userProfiles = testUserProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        UserProfile user1 =
            testUserProfileRepository.save(
                new UserProfile(
                    String.valueOf(new Random().nextInt()),
                    "user1firstname.user1Lastname@somewhere.com",
                    "joe",
                    "bloggs"));

        userProfileMap = new HashMap<>();
        userProfileMap.put("user1", user1);

    }

    @Test
    public void should_retrieve_user_profile_resource_with_id() throws Exception {
        UserProfile userProfile = userProfileMap.get("user1");

        UserProfileResource retrievedResource =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + SLASH + userProfile.getId(),
                OK,
                UserProfileResource.class
            );

        assertThat(retrievedResource).isNotNull();
        assertThat(retrievedResource).isEqualToComparingFieldByField(userProfile);

    }

    @Test
    public void should_return_404_when_user_profile_id_not_in_the_db() throws Exception {

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + SLASH + UUID.randomUUID().toString(),
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }

    @Test
    public void should_return_404_when_nothing_in_the_db() throws Exception {

        testUserProfileRepository.delete(userProfileMap.get("user1"));
        Iterable<UserProfile> userProfiles = testUserProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + SLASH + UUID.randomUUID().toString(),
                NOT_FOUND
            );
        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }

    @Test
    public void should_retrieve_user_profile_resource_with_email() throws Exception {
        UserProfile userProfile = userProfileMap.get("user1");
        String path = APP_BASE_PATH + "?email=" + userProfile.getEmail();

        UserProfileResource retrievedResource =
            testRequestHandler.sendGet(
                mockMvc,
                path,
                OK,
                UserProfileResource.class
            );

        assertThat(retrievedResource).isEqualToComparingFieldByField(userProfile);

    }

    @Test
    public void should_return_404_when_user_profile_email_not_in_the_db() throws Exception {

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=" + "randomemail@somewhere.com",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }

    @Test
    public void should_return_404_when_query_by_email_and_nothing_in_the_db() throws Exception {

        testUserProfileRepository.delete(userProfileMap.get("user1"));
        Iterable<UserProfile> userProfiles = testUserProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=" + "randomemail@somewhere.com",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }

    @Test
    public void should_return_404_when_query_by_email_is_empty() throws Exception {
        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }


    @Test
    public void should_retrieve_user_profile_resource_with_idamId() throws Exception {
        UserProfile userProfile = userProfileMap.get("user1");
        String path = APP_BASE_PATH + "?idamId=" + userProfile.getIdamId();

        UserProfileResource retrievedResource =
            testRequestHandler.sendGet(
                mockMvc,
                path,
                OK,
                UserProfileResource.class
            );

        assertThat(retrievedResource).isNotNull();
        assertThat(retrievedResource).isEqualToComparingFieldByField(userProfile);

    }

    @Test
    public void should_return_404_when_query_by_idamId_is_empty() throws Exception {

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + "?idamId=",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }

}
