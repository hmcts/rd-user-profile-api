package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;



@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
@SpringBootTest
@Ignore
public class RetrieveUserProfileFuncTest extends AbstractFunctional {

    private static final Logger LOG = LoggerFactory.getLogger(RetrieveUserProfileFuncTest.class);

    @Before
    public void setUp() {
        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
    }

    @Test
    public void should_get_user_profile_by_userId() throws Exception {

        UserProfileCreationData userProfileCreationData = createUserProfileData();
        UserProfileCreationResponse createdResource = createUserProfile(userProfileCreationData, HttpStatus.CREATED);
        UserProfileResponse resource =
            testRequestHandler.sendGet(
                requestUri + "?userId=" + createdResource.getIdamId(),
                    UserProfileResponse.class);

        verifyGetUserProfile(resource, userProfileCreationData);
    }

    @Test
    public void should_get_user_profile_with_roles_by_userId() throws Exception {

        UserProfileCreationData userProfileCreationData = createUserProfileData();
        UserProfileCreationResponse createdResource = createUserProfile(userProfileCreationData, HttpStatus.CREATED);
        UserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + createdResource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        verifyGetUserProfileWithRoles(resource, userProfileCreationData);
    }

    @Test
    public void should_get_user_profile_by_email_from_header() throws Exception {

        UserProfileCreationData userProfileCreationData = createUserProfileData();
        UserProfileCreationResponse createdResource = createUserProfile(userProfileCreationData, HttpStatus.CREATED);

        UserProfileResponse resource =
                testRequestHandler.getEmailFromHeader(
                        requestUri + "?email=" + "up@prdfunctestuser.com",
                        UserProfileResponse.class,
                        userProfileCreationData.getEmail().toLowerCase()
                );

        verifyGetUserProfile(resource, userProfileCreationData);
        assertThat(createdResource.getIdamId()).isNotNull();
    }
}
