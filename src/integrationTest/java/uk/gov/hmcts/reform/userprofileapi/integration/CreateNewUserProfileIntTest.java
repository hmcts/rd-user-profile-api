package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.List;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileIntTest extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUp() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }



    @Test
    public void should_return_201_and_create_user_profile_resource() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
            userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                CREATED,
                UserProfileCreationResponse.class
            );

        verifyUserProfileCreation(createdResource, CREATED, data);
    }



    @Test
    public void should_return_400_and_not_create_user_profile_when_empty_body() throws Exception {

        MvcResult result =
            userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                "{}",
                BAD_REQUEST
            );

        assertThat(result.getResponse().getContentAsString()).isNotEmpty();
    }


    @Test
    public void should_return_400_when_any_mandatory_field_missing() throws Exception {

        List<String> mandatoryFieldList =
            Lists.newArrayList(
                "email",
                "firstName",
                "lastName",
                "roles"
            );

        new JSONObject(
            objectMapper.writeValueAsString(
                    buildCreateUserProfileData()
            )
        );


        mandatoryFieldList.forEach(s -> {

            try {

                JSONObject jsonObject =
                    new JSONObject(objectMapper.writeValueAsString(buildCreateUserProfileData()));

                jsonObject.remove(s);

                mockMvc.perform(post(APP_BASE_PATH)
                    .content(jsonObject.toString())
                    .contentType(APPLICATION_JSON_VALUE))
                    .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                    .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

    @Test
    public void should_return_400_when_fields_are_blank_or_having_only_whitespaces() throws Exception {

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
                        buildCreateUserProfileData()
                )
        );

        mandatoryFieldList.forEach(s -> {

            try {

                JSONObject jsonObject =
                        new JSONObject(objectMapper.writeValueAsString(buildCreateUserProfileData()));

                jsonObject.put(s,"");

                mockMvc.perform(post(APP_BASE_PATH)
                        .content(jsonObject.toString())
                        .contentType(APPLICATION_JSON_VALUE))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();

                jsonObject.put(s," ");

                mockMvc.perform(post(APP_BASE_PATH)
                        .content(jsonObject.toString())
                        .contentType(APPLICATION_JSON_VALUE))
                        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()))
                        .andReturn();

            } catch (Exception e) {
                Assertions.fail("could not run test correctly", e);
            }

        });

    }

    @Test
    public void should_return_201_and_create_user_profile_resource_with_allowed_email() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        data.setEmail("a.adison@gmail.com");

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, data);
    }

    @Test
    public void should_return_400_and_create_user_profile_resource_with_invalid_email() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        data.setEmail("a.adisongmail.com");


        userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                BAD_REQUEST,
                UserProfileCreationResponse.class
        );

    }

    @Test
    public void should_return_400_and_create_user_profile_resource_with_invalid_email_1() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        data.setEmail("a.adison@gmailcom");


        userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                BAD_REQUEST,
                UserProfileCreationResponse.class
        );



    }

}
