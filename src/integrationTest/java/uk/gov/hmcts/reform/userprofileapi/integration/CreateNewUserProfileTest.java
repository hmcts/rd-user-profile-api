package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.TestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.repository.UserProfileRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileTest {

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TestRequestHandler testRequestHandler;

    private Map<String, UserProfile> userProfileMap;

    private MockMvc mockMvc;

    private static final String APP_BASE_PATH = "/profiles";
    private static final String SLASH = "/";

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();

        Iterable<UserProfile> userProfiles = userProfileRepository.findAll();
        assertThat(userProfiles).isEmpty();

        UserProfile user1 =
            userProfileRepository.save(
                new UserProfile(
                    String.valueOf(new Random().nextInt()),
                    "user1firstname.user1Lastname@somewhere.com",
                    "joe",
                    "bloggs"));

        userProfileMap = new HashMap<>();
        userProfileMap.put("user1", user1);

    }

    @Test
    public void should_create_user_profile_resource() throws Exception {

        CreateUserProfileData data =
            new CreateUserProfileData("joe.bloggs@somewhere.com", "joe", "bloggs");

        UserProfileResource createdResource =
            testRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                CREATED,
                UserProfileResource.class
            );

        assertThat(createdResource).isEqualToIgnoringGivenFields(createdResource, "id");
        assertThat(createdResource.getId()).isNotNull();
        assertThat(createdResource.getId()).isInstanceOf(UUID.class);

    }

    @Test
    public void should_not_create_user_profile_when_empty_body() throws Exception {

        CreateUserProfileData data = new CreateUserProfileData();

        assertThatThrownBy(() ->
            testRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                BAD_REQUEST,
                UserProfileResource.class
            )).hasMessageContaining("cannot be null")
            .hasCauseInstanceOf(NullPointerException.class);

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

        assertThat(retrievedResource).isNotNull();
        assertThat(retrievedResource).isEqualToComparingFieldByField(userProfile);

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

}
