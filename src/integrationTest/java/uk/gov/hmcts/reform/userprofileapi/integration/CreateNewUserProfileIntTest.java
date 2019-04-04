package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.util.UUID;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.TestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileIntTest {

    private MockMvc mockMvc;

    private static final String APP_BASE_PATH = "/profiles";

    @Autowired
    private TestRequestHandler testRequestHandler;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_return_201_and_create_user_profile_resource() throws Exception {

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

        assertThat(createdResource).isEqualToIgnoringGivenFields(data, "id", "idamId");
        assertThat(createdResource.getId()).isNotNull();
        assertThat(createdResource.getId()).isInstanceOf(UUID.class);
        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(String.class);

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body() {

        CreateUserProfileData data = mock(CreateUserProfileData.class);

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
    public void should_return_400_when_mandatory_field_missing() throws Exception {

        String json = "{\"firstName\":\"iWvKhGLXCiOMMbZtngbR\",\"lastName\":\"mXlpNLcbodhABAWKCKbj\"}";

        mockMvc.perform(post(APP_BASE_PATH)
            .content(json)
            .contentType(APPLICATION_JSON_UTF8))
            .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
            .andReturn();

    }

}
