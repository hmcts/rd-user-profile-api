package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildUpdateUserProfileData;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.client.GetUserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.client.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;

@ContextConfiguration(classes = {TestConfigProperties.class, FuncTestRequestHandler.class})
@ComponentScan("uk.gov.hmcts.reform.userprofileapi")
@TestPropertySource("classpath:application-functional.yaml")
public class AbstractFunctional {

    @Value("${targetInstance}") protected String targetInstance;

    @Autowired
    protected FuncTestRequestHandler testRequestHandler;

    protected String requestUri = "/v1/userprofile";

    @Value("${exui.role.hmcts-admin}")
    protected String hmctsAdmin;
    @Value("${exui.role.pui-user-manager}")
    protected String puiUserManager;
    @Value("${exui.role.pui-organisation-manager}")
    protected String puiOrgManager;
    @Value("${exui.role.pui-finance-manager}")
    protected String puiFinanceManager;
    @Value("${exui.role.pui-case-manager}")
    protected String puiCaseManager;

    /*@Before
    public void setupProxy() {
        //TO enable for local testing
        RestAssured.proxy("proxyout.reform.hmcts.net",8080);
        SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);
    }*/


    protected CreateUserProfileResponse createUserProfile(CreateUserProfileData createUserProfileData,HttpStatus expectedStatus) throws Exception {

        CreateUserProfileResponse resource = testRequestHandler.sendPost(
                createUserProfileData,
                expectedStatus,
                requestUri,
                CreateUserProfileResponse.class
        );
        verifyCreateUserProfile(resource);
        return resource;
    }

    protected void updateUserProfile(UpdateUserProfileData updateUserProfileData, String userId, HttpStatus expectedStatus) throws Exception {

        testRequestHandler.sendPut(
                updateUserProfileData,
                expectedStatus,
                requestUri + "/" + userId);
    }

    protected CreateUserProfileData createUserProfileData() {
        return buildCreateUserProfileData();
    }

    protected UpdateUserProfileData updateUserProfileData() {
        return buildUpdateUserProfileData();
    }

    protected void verifyCreateUserProfile(CreateUserProfileResponse resource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull();
        assertThat(resource.getIdamId()).isInstanceOf(String.class);
    }

    protected void verifyGetUserProfile(GetUserProfileResponse resource, CreateUserProfileData expectedResource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull().isExactlyInstanceOf(String.class);
        assertThat(resource.getFirstName()).isEqualTo(expectedResource.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedResource.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedResource.getEmail().toLowerCase());
        assertThat(resource.getIdamStatus()).isNotNull();
    }

    protected void verifyGetUserProfileWithRoles(GetUserProfileWithRolesResponse resource, CreateUserProfileData expectedResource) {

        verifyGetUserProfile(resource, expectedResource);
        //assertThat(resource.getRoles()).isNotEmpty();

    }

}