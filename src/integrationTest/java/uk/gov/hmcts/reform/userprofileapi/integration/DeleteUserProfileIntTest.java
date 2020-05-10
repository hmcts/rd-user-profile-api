package uk.gov.hmcts.reform.userprofileapi.integration;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import java.util.ArrayList;
import java.util.List;

import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

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
        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource.getIdamId());
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        userProfileRequestHandlerTest.sendDelete(mockMvc,
                APP_BASE_PATH,
                deletionRequest,
                NO_CONTENT,
                UserProfilesDeletionResponse.class);
    }

    @Test
    public void should_return_204_and_delete_multiple_user_profile_resource() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        //user profile one created
        UserProfileCreationResponse createdResourceOne =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResourceOne, CREATED, data);
        UserProfileCreationData data2 = buildCreateUserProfileData();
        //user profile two created
        UserProfileCreationResponse createdResourceTwo =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data2,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResourceOne.getIdamId());
        userIds.add(createdResourceTwo.getIdamId());
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        //user profiles deleted
        userProfileRequestHandlerTest.sendDelete(mockMvc,
                APP_BASE_PATH,
                deletionRequest,
                NO_CONTENT,
                UserProfilesDeletionResponse.class);
    }

    @Test
    public void should_return_404_when_no_user_profile_resource_to_delte() throws Exception {
        List<String> userIds = new ArrayList<String>();
        userIds.add("123456");
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        userProfileRequestHandlerTest.sendDelete(mockMvc,
                APP_BASE_PATH,
                deletionRequest,
                NOT_FOUND,
                UserProfilesDeletionResponse.class);
    }

    @Test
    public void should_return_400_when_empty_request_to_delete_user_profile_resource() throws Exception {
        List<String> userIds = new ArrayList<String>();
        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);
        userProfileRequestHandlerTest.sendDelete(mockMvc,
                APP_BASE_PATH,
                deletionRequest,
                BAD_REQUEST,
                UserProfilesDeletionResponse.class);
    }

    @After
    public void tearDown() {

    }
}
