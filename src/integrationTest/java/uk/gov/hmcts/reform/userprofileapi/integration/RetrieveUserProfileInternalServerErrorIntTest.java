package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

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
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRequestHandlerTest;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.service.IdamServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class RetrieveUserProfileInternalServerErrorIntTest extends AuthorizationEnabledIntegrationTest {

    private static final String APP_BASE_PATH = "/v1/userprofile";
    private static final String SLASH = "/";
    @MockBean
    protected IdamServiceImpl idamService;

    @MockBean
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserProfileRequestHandlerTest userProfileRequestHandlerTest;

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_return_500_and_not_create_user_profile_when_idam_service_throws_exception() throws Exception {

        when(idamService.registerUser(any(CreateUserProfileData.class)))
            .thenThrow(new RuntimeException("This is a test exception"));

        CreateUserProfileData data = buildCreateUserProfileData();

        MvcResult result = userProfileRequestHandlerTest.sendPost(
            mockMvc,
            APP_BASE_PATH,
            data,
            INTERNAL_SERVER_ERROR
        );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_500_when_repository_throws_an_unknown_exception() throws Exception {

        when(idamService.registerUser(any(CreateUserProfileData.class)))
            .thenReturn(new IdamRegistrationInfo(CREATED, null));
        when(userProfileRepository.findByIdamId(any(UUID.class)))
            .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?" + "userId=" + UUID.randomUUID(),
                INTERNAL_SERVER_ERROR
            );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_500_when_query_by_email_and_repository_throws_an_unknown_exception() throws Exception {

        when(idamService.registerUser(any(CreateUserProfileData.class)))
            .thenReturn(new IdamRegistrationInfo(ACCEPTED, null));
        when(userProfileRepository.findByEmail(anyString()))
            .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?email=" + "randomemail@somewhere.com",
                INTERNAL_SERVER_ERROR
            );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_500_when_query_by_userId_and_repository_throws_an_unknown_exception() throws Exception {

        when(idamService.registerUser(any(CreateUserProfileData.class)))
            .thenReturn(new IdamRegistrationInfo(ACCEPTED, null));
        when(userProfileRepository.findByIdamId(any(UUID.class)))
            .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result =
            userProfileRequestHandlerTest.sendGet(
                mockMvc,
                APP_BASE_PATH + "?userId=" + UUID.randomUUID(),
                INTERNAL_SERVER_ERROR
            );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }


}
