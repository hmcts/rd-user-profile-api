package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesRequest;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfilesResponse;
import uk.gov.hmcts.reform.userprofileapi.service.IdamStatus;

@Ignore
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
        userIds.add(createdResource1.getIdamId().toString());
        userIds.add(createdResource2.getIdamId().toString());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);


        GetUserProfilesResponse response = testRequestHandler.sendPost(
                request,
                HttpStatus.OK,
                requestUri + "/users?showdeleted=false",
                GetUserProfilesResponse.class
        );

        assertThat(response.getUserProfiles().size()).isEqualTo(2);
        GetUserProfileWithRolesResponse getUserProfileWithRolesResponse1 = response.getUserProfiles().get(0);
        GetUserProfileWithRolesResponse getUserProfileWithRolesResponse2 = response.getUserProfiles().get(1);

        assertThat(getUserProfileWithRolesResponse1.getEmail()).isEqualTo(createUserProfileData1.getEmail().toLowerCase());
        assertThat(getUserProfileWithRolesResponse1.getFirstName()).isEqualTo(createUserProfileData1.getFirstName());
        assertThat(getUserProfileWithRolesResponse1.getLastName()).isEqualTo(createUserProfileData1.getLastName());
        assertThat(getUserProfileWithRolesResponse1.getIdamStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(getUserProfileWithRolesResponse1.getRoles()).isNotEmpty();

        assertThat(getUserProfileWithRolesResponse2.getEmail()).isEqualTo(createUserProfileData2.getEmail().toLowerCase());
        assertThat(getUserProfileWithRolesResponse2.getFirstName()).isEqualTo(createUserProfileData2.getFirstName());
        assertThat(getUserProfileWithRolesResponse2.getLastName()).isEqualTo(createUserProfileData2.getLastName());
        assertThat(getUserProfileWithRolesResponse2.getIdamStatus()).isEqualTo(IdamStatus.PENDING);
        assertThat(getUserProfileWithRolesResponse2.getRoles()).isNotEmpty();
    }

    @Test
    public void should_get_multiple_users_profile_with_param_showdeleted_true() throws Exception {

        CreateUserProfileData createUserProfileData1 = createUserProfileData();
        CreateUserProfileData createUserProfileData2 = createUserProfileData();
        CreateUserProfileResponse createdResource1 = createUserProfile(createUserProfileData1, HttpStatus.CREATED);
        CreateUserProfileResponse createdResource2 = createUserProfile(createUserProfileData2, HttpStatus.CREATED);

        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource1.getIdamId().toString());
        userIds.add(createdResource2.getIdamId().toString());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);

        GetUserProfilesResponse response = testRequestHandler.sendPost(
                request,
                HttpStatus.OK,
                requestUri + "/users?showdeleted=true",
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
                requestUri + "/users?showdeleted=false"
        );
    }

    @Test
    public void should_return_400_when_param_invalid() throws Exception {

        List<String> userIds = new ArrayList<String>();
        userIds.add(UUID.randomUUID().toString());
        userIds.add(UUID.randomUUID().toString());

        GetUserProfilesRequest request = new GetUserProfilesRequest(userIds);

        testRequestHandler.sendPost(
                request,
                HttpStatus.BAD_REQUEST,
                requestUri + "/users?showdeleted=false"
        );
    }
}
