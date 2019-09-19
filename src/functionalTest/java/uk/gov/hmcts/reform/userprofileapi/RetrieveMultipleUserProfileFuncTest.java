package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesRequest;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesResponse;


@RunWith(SpringIntegrationSerenityRunner.class)
@SpringBootTest
public class RetrieveMultipleUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveMultipleUserProfileFuncTest.class);

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_get_multiple_users_profile_with_param_showdeleted_false() throws Exception {

        CreateUserProfileData createUserProfileData1 = createUserProfileData();
        CreateUserProfileData createUserProfileData2 = createUserProfileData();
        CreateUserProfileResponse createdResource1 = createUserProfile(createUserProfileData1, HttpStatus.CREATED);
        CreateUserProfileResponse createdResource2 = createUserProfile(createUserProfileData2, HttpStatus.CREATED);

        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource1.getIdamId());
        userIds.add(createdResource2.getIdamId());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);


        GetUserProfilesResponse response = testRequestHandler.sendPost(
                request,
                HttpStatus.OK,
                requestUri + "/users?showdeleted=false&rolesRequired=true",
                GetUserProfilesResponse.class
        );

        assertThat(response.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    public void should_get_multiple_users_profile_with_param_showdeleted_true() throws Exception {

        CreateUserProfileData createUserProfileData1 = createUserProfileData();
        CreateUserProfileData createUserProfileData2 = createUserProfileData();
        CreateUserProfileResponse createdResource1 = createUserProfile(createUserProfileData1, HttpStatus.CREATED);
        CreateUserProfileResponse createdResource2 = createUserProfile(createUserProfileData2, HttpStatus.CREATED);

        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource1.getIdamId());
        userIds.add(createdResource2.getIdamId());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);

        GetUserProfilesResponse response = testRequestHandler.sendPost(
                request,
                HttpStatus.OK,
                requestUri + "/users?showdeleted=true&rolesRequired=true",
                GetUserProfilesResponse.class
        );

        assertThat(response.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    public void should_return_404_when_no_users_profiles_in_db() throws Exception {

        List<String> userIds = new ArrayList<String>();
        userIds.add(UUID.randomUUID().toString());
        userIds.add(UUID.randomUUID().toString());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);

        testRequestHandler.sendPost(
                request,
                HttpStatus.NOT_FOUND,
                requestUri + "/users?showdeleted=false&rolesRequired=true"
        );
    }

    @Test
    public void should_return_404_when_param_invalid() throws Exception {

        List<String> userIds = new ArrayList<String>();
        userIds.add(UUID.randomUUID().toString());
        userIds.add(UUID.randomUUID().toString());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);

        testRequestHandler.sendPost(
                request,

                HttpStatus.BAD_REQUEST,
                requestUri + "/users?showdeleted=fals&rolesRequired=true"

        );
    }
}
