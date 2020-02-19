package uk.gov.hmcts.reform.userprofileapi;

import io.restassured.RestAssured;
import java.util.UUID;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;

@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public class UpdateUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(UpdateUserProfileFuncTest.class);
    UserProfileCreationResponse createdResource;

    @Before
    public void setUp() throws Exception {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        createdResource = createUserProfile(createUserProfileData(), HttpStatus.CREATED);
    }

    @Test
    public void should_create_Update_profile_and_return_200() throws Exception {
        updateUserProfile(updateUserProfileData(), createdResource.getIdamId(), HttpStatus.OK);
    }

    @Test
    public void should_throw_400_when_Update_profile_with_invalid_first_name() throws Exception {
        UpdateUserProfileData updateUserProfileData = updateUserProfileData();
        updateUserProfileData.setFirstName("<This!Is$Invalid");

        updateUserProfile(updateUserProfileData, createdResource.getIdamId(), HttpStatus.BAD_REQUEST);

        updateUserProfileData.setFirstName("%3cscript%3ealert(%22WXSS%22)%3c%2fscript%3e");

        updateUserProfile(updateUserProfileData, createdResource.getIdamId(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void should_throw_400_when_Update_profile_with_invalid_last_name() throws Exception {
        UpdateUserProfileData updateUserProfileData = updateUserProfileData();
        updateUserProfileData.setLastName("<This!Is$Invalid");

        updateUserProfile(updateUserProfileData, createdResource.getIdamId(), HttpStatus.BAD_REQUEST);

        updateUserProfileData.setLastName("%3cscript%3ealert(%22WXSS%22)%3c%2fscript%3e");

        updateUserProfile(updateUserProfileData, createdResource.getIdamId(), HttpStatus.BAD_REQUEST);
    }

    @Test
    public void should_throw_404_while_update_profile_with_userId_not_in_db() throws Exception {
        updateUserProfile(updateUserProfileData(), UUID.randomUUID().toString(), HttpStatus.NOT_FOUND);
    }
}
