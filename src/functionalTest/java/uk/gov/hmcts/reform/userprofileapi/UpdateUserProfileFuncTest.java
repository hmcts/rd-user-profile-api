package uk.gov.hmcts.reform.userprofileapi;

import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.IdamClient;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public class UpdateUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateUserProfileFuncTest.class);
    CreateUserProfileResponse createdResource;
    private IdamClient idamClient;
    @Autowired
    protected TestConfigProperties configProperties;

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        idamClient = new IdamClient(configProperties);
        createdResource = createUserProfile(createUserProfileData(), HttpStatus.CREATED);
    }

    @Test
    public void should_create_Update_profile_and_return_200() throws Exception {
        updateUserProfile(updateUserProfileData(), createdResource.getIdamId(), HttpStatus.OK);
    }

    @Test
    public void should_throw_404_while_update_profile_with_userId_not_in_db() throws Exception {
        updateUserProfile(updateUserProfileData(), UUID.randomUUID().toString(), HttpStatus.NOT_FOUND);
    }

    @Test
    public void should_update_user_details() throws Exception {

        List<String> xuiuRoles = new ArrayList<String>();
        xuiuRoles.add("pui-user-manager");
        xuiuRoles.add("pui-case-manager");

        //create user with "pui-user-manager" role in SIDAM
        List<String> sidamRoles = new ArrayList<>();
        sidamRoles.add("pui-user-manager");
        String email = idamClient.createUser(sidamRoles);

        //create User profile with same email to get 409 scenario
        CreateUserProfileData data = createUserProfileData();
        data.setRoles(xuiuRoles);
        data.setEmail(email);
        CreateUserProfileResponse duplicateUserResource = createUserProfile(data, HttpStatus.CREATED);
        verifyCreateUserProfile(duplicateUserResource);

        String userId = duplicateUserResource.getIdamId();

        UpdateUserProfileData updateData = new UpdateUserProfileData();
        updateData.setFirstName("fname1");
        updateData.setLastName("lname1");
        updateData.setIdamStatus("SUSPENDED");

        UserProfileRolesResponse resource1 =
                testRequestHandler.sendPut(
                        updateData,
                        HttpStatus.OK,
                        requestUri + "/" + userId + "?origin=exui", UserProfileRolesResponse.class);
    }
}
