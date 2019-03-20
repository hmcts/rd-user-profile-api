package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileDataMandatoryFieldsOnly;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.reform.userprofileapi.client.IntTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.infrastructure.clients.UserProfileResource;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileIntTest {

    private MockMvc mockMvc;

    private static final String APP_BASE_PATH = "/profiles";

    @Autowired
    private IntTestRequestHandler intTestRequestHandler;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    //fields set internally by the api
    private List<String> apiGeneratedFields =
        Lists.newArrayList(
            "id",
            "emailCommsConsentTs",
            "postalCommsConsentTs",
            "creationChannel",
            "idamId",
            "idamStatus"
        );

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void should_return_201_and_create_user_profile_resource() throws Exception {

        CreateUserProfileData data =
            buildCreateUserProfileData();
        IdamRegistrationInfo idamInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
        UserProfile userProfile = new UserProfile(data, idamInfo);

        UserProfileResource expectedResource = new UserProfileResource(userProfile);

        UserProfileResource createdResource =
            intTestRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                CREATED,
                UserProfileResource.class
            );

        verifyUserProfileResource(createdResource, expectedResource);

    }

    @Test
    public void should_return_201_when_only_mandatory_fields_sent() throws Exception {

        CreateUserProfileData data = buildCreateUserProfileDataMandatoryFieldsOnly();
        IdamRegistrationInfo idamInfo = new IdamRegistrationInfo(HttpStatus.ACCEPTED);
        UserProfile userProfile = new UserProfile(data, idamInfo);

        UserProfileResource expectedResource = new UserProfileResource(userProfile);

        UserProfileResource createdResource =
            intTestRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                CREATED,
                UserProfileResource.class
            );

        verifyUserProfileResource(createdResource, expectedResource);

    }

    private void verifyUserProfileResource(UserProfileResource createdResource, UserProfileResource expectedResource) {

        assertThat(createdResource).isEqualToIgnoringGivenFields(expectedResource,
            apiGeneratedFields.toArray(new String[apiGeneratedFields.size()]));

        assertThat(createdResource.getId()).isNotNull();
        assertThat(createdResource.getId()).isInstanceOf(UUID.class);
        assertThat(createdResource.getEmailCommsConsentTs())
            .isBetween(LocalDateTime.now().minusSeconds(30), LocalDateTime.now());
        assertThat(createdResource.getPostalCommsConsentTs())
            .isBetween(LocalDateTime.now().minusSeconds(30), LocalDateTime.now());

        //not currently being populated
        assertThat(createdResource.getIdamId()).isNull();
        assertThat(createdResource.getIdamStatus()).isNull();

    }

    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body() throws Exception {

        MvcResult result =
            intTestRequestHandler.sendPost(
                mockMvc,
                APP_BASE_PATH,
                "{}",
                BAD_REQUEST
            );

        assertThat(result.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    public void should_return_400_when_any_mandatory_field_missing() throws Exception {

        List<String> mandatoryFieldList =
            Lists.newArrayList(
                "email",
                "firstName",
                "lastName",
                "userCategory",
                "userType"
            );

        new JSONObject(
            objectMapper.writeValueAsString(
                buildCreateUserProfileDataMandatoryFieldsOnly()
            )
        );

        mandatoryFieldList.forEach(s -> {

            try {

                JSONObject jsonObject =
                    new JSONObject(objectMapper.writeValueAsString(buildCreateUserProfileDataMandatoryFieldsOnly()));

                jsonObject.remove(s);

                mockMvc.perform(post(APP_BASE_PATH)
                    .content(jsonObject.toString())
                    .contentType(APPLICATION_JSON_UTF8))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

}
