package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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
public class RetrieveUserProfileInternalServerErrorIntTest {

    private static final String APP_BASE_PATH = "/profiles";
    private static final String SLASH = "/";

    @MockBean
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TestRequestHandler testRequestHandler;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_return_500_and_not_create_user_profile_when_app_throws_internal_exception() {

        when(userProfileRepository.findById(any(UUID.class)))
            .thenThrow(new RuntimeException("This is a test exception"));

        CreateUserProfileData data = new CreateUserProfileData();

        assertThatThrownBy(() ->
            testRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                BAD_REQUEST,
                UserProfileResource.class
            ));

    }

    @Test
    public void should_return_500_when_repository_throws_an_unknown_exception() throws Exception {

        when(userProfileRepository.findById(any(UUID.class)))
            .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + SLASH + UUID.randomUUID().toString(),
                INTERNAL_SERVER_ERROR
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();

    }

    @Test
    public void should_return_500_when_query_by_email_and_repository_throws_an_unknown_exception() throws Exception {

        when(userProfileRepository.save(any(UserProfile.class)))
            .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result =
            testRequestHandler.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=" + "randomemail@somewhere.com",
                NOT_FOUND
            );

        assertThat(result.getResponse()).isNotNull();
        assertThat(result.getResponse().getContentAsString()).isEmpty();


    }


}
