package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRequestHandlerTest;
import uk.gov.hmcts.reform.userprofileapi.controller.request.IdamRegisterUserRequest;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;

@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class RetrieveUserProfileInternalServerErrorIntTest extends AuthorizationEnabledIntegrationTest {

    private static final String APP_BASE_PATH = "/v1/userprofile";
    private static final String SLASH = "/";
    @MockBean
    protected IdamService idamService;

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

        when(idamService.registerUser(any(IdamRegisterUserRequest.class)))
                .thenThrow(new RuntimeException("Runtime Exception"));

        UserProfileCreationData data = buildCreateUserProfileData();

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

        IdamRegisterUserRequest request = Mockito.mock(IdamRegisterUserRequest.class);
        IdamRegistrationInfo idamRegistrationInfo = new IdamRegistrationInfo(ResponseEntity.status(CREATED).build());
        when(idamService.registerUser(request))
                .thenReturn(idamRegistrationInfo);
        when(userProfileRepository.findByIdamId(any(String.class)))
                .thenThrow(new RuntimeException("Runtime Exception"));

        MvcResult result =
                userProfileRequestHandlerTest.sendGet(
                        mockMvc,
                        APP_BASE_PATH + "?" + "userId=" + UUID.randomUUID().toString(),
                        INTERNAL_SERVER_ERROR
                );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();

    }

    @Test
    public void should_return_500_when_query_by_email_and_repository_throws_an_unknown_exception() throws Exception {
        when(userProfileRepository.findByEmail(anyString()))
                .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result = userProfileRequestHandlerTest.sendGet(mockMvc, APP_BASE_PATH + "?email="
                .concat("randomemail@somewhere.com"), INTERNAL_SERVER_ERROR);

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    public void should_return_500_when_email_from_header_and_repository_throws_an_unknown_exception() throws Exception {
        when(userProfileRepository.findByEmail(anyString()))
                .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result = userProfileRequestHandlerTest.sendGetFromHeader(mockMvc, APP_BASE_PATH + "?email="
                .concat("randomemail@somewhere.com"), INTERNAL_SERVER_ERROR,"randomemail@somewhere.com");

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }

    @Test
    public void should_return_500_when_query_by_userId_and_repository_throws_an_unknown_exception() throws Exception {
        when(userProfileRepository.findByIdamId(any(String.class)))
                .thenThrow(new RuntimeException("This is a test exception"));

        MvcResult result = userProfileRequestHandlerTest.sendGet(mockMvc, APP_BASE_PATH + "?userId="
                + UUID.randomUUID().toString(), INTERNAL_SERVER_ERROR);

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }
}
