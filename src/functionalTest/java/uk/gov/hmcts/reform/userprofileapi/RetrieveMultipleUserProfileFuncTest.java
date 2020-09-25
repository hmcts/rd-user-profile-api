package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
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

        UserProfileCreationData userProfileCreationData1 = createUserProfileData();
        UserProfileCreationData userProfileCreationData2 = createUserProfileData();
        UserProfileCreationResponse createdResource1 = createUserProfile(userProfileCreationData1, HttpStatus.CREATED);
        UserProfileCreationResponse createdResource2 = createUserProfile(userProfileCreationData2, HttpStatus.CREATED);

        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource1.getIdamId());
        userIds.add(createdResource2.getIdamId());

        UserProfileDataRequest request = new UserProfileDataRequest(userIds);


        UserProfileDataResponse response = testRequestHandler.sendPost(
                request,
                HttpStatus.OK,
                requestUri + "/users?showdeleted=false&rolesRequired=true",
                UserProfileDataResponse.class
        );

        assertThat(response.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    public void should_get_multiple_users_profile_with_param_showdeleted_true() throws Exception {

        UserProfileCreationData userProfileCreationData1 = createUserProfileData();
        UserProfileCreationData userProfileCreationData2 = createUserProfileData();
        UserProfileCreationResponse createdResource1 = createUserProfile(userProfileCreationData1, HttpStatus.CREATED);
        UserProfileCreationResponse createdResource2 = createUserProfile(userProfileCreationData2, HttpStatus.CREATED);

        List<String> userIds = new ArrayList<String>();
        userIds.add(createdResource1.getIdamId());
        userIds.add(createdResource2.getIdamId());

        UserProfileDataRequest request = new UserProfileDataRequest(userIds);

        UserProfileDataResponse response = testRequestHandler.sendPost(
                request,
                HttpStatus.OK,
                requestUri + "/users?showdeleted=true&rolesRequired=true",
                UserProfileDataResponse.class
        );

        assertThat(response.getUserProfiles().size()).isEqualTo(2);
    }

    @Test
    public void should_return_404_when_no_users_profiles_in_db() throws Exception {

        List<String> userIds = new ArrayList<String>();
        userIds.add(UUID.randomUUID().toString());
        userIds.add(UUID.randomUUID().toString());

        UserProfileDataRequest request = new UserProfileDataRequest(userIds);

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

        UserProfileDataRequest request = new UserProfileDataRequest(userIds);

        testRequestHandler.sendPost(
                request,

                HttpStatus.BAD_REQUEST,
                requestUri + "/users?showdeleted=fals&rolesRequired=true"

        );
    }
}
