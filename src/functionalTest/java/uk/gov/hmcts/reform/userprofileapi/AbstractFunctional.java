package uk.gov.hmcts.reform.userprofileapi;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.client.IdamOpenIdClient;
import uk.gov.hmcts.reform.userprofileapi.client.S2sClient;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ContextConfiguration(classes = {TestConfigProperties.class})
@ComponentScan("uk.gov.hmcts.reform.userprofileapi")
@TestPropertySource("classpath:application-functional.yaml")
@TestExecutionListeners(listeners = {
        AbstractFunctional.class,
        DependencyInjectionTestExecutionListener.class})
@Slf4j
public class AbstractFunctional extends AbstractTestExecutionListener {

    @Autowired
    protected TestConfigProperties configProperties;

    protected String requestUri = "/v1/userprofile";
    @Value("${targetInstance}")
    protected String targetInstance;
    @Value("${s2s.auth.secret}")
    protected String s2sSecret;
    @Value("${s2s.auth.url}")
    protected String s2sBaseUrl;
    @Value("${s2s.auth.microservice:rd_user_profile_api}")
    protected String s2sMicroservice;
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
    @Value("${resendInterval}")
    protected String resendInterval;

    public static final String EMAIL = "EMAIL";
    public static final String CREDS = "CREDS";
    protected static FuncTestRequestHandler testRequestHandler;
    protected static IdamOpenIdClient idamOpenIdClient;
    protected static String s2sToken;

    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);

        RestAssured.useRelaxedHTTPSValidation();

        if (null == s2sToken) {
            log.info(":::: Generating S2S Token");
            s2sToken = new S2sClient(s2sBaseUrl, s2sMicroservice, s2sSecret).getS2sToken();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(configProperties);
        }

        testRequestHandler = new FuncTestRequestHandler(targetInstance, s2sToken, idamOpenIdClient);
    }

    protected UserProfileCreationResponse createUserProfile(
            UserProfileCreationData userProfileCreationData) throws Exception {

        UserProfileCreationResponse resource = testRequestHandler.sendPost(
                userProfileCreationData,
                HttpStatus.CREATED,
                requestUri,
                UserProfileCreationResponse.class
        );
        verifyCreateUserProfile(resource);
        return resource;
    }

    protected UserProfileCreationResponse createActiveUserProfileWithGivenFields(
            UserProfileCreationData userProfileCreationData) throws Exception {
        List<String> xuiuRoles = new ArrayList();
        xuiuRoles.add("pui-user-manager");
        xuiuRoles.add("pui-case-manager");

        //create user with "pui-user-manager" role in SIDAM
        List<String> sidamRoles = new ArrayList<>();
        sidamRoles.add("pui-user-manager");
        Map<String, String> userCreds = idamOpenIdClient.createUserWithGivenFields(sidamRoles, userProfileCreationData);

        //create User profile with same email to get 409 scenario
        userProfileCreationData.setRoles(xuiuRoles);
        userProfileCreationData.setEmail(userCreds.get(EMAIL));
        return createUserProfile(userProfileCreationData);
    }

    protected void updateUserProfile(UpdateUserProfileData updateUserProfileData, String userId) throws Exception {

        testRequestHandler.sendPut(
                updateUserProfileData,
                HttpStatus.OK,
                requestUri + "/" + userId);
    }

    protected UserProfileCreationData createUserProfileData() {
        return buildCreateUserProfileData();
    }

    protected UserProfileCreationData createUserProfileDataWithReInvite() {

        return buildCreateUserProfileData(true);
    }

    protected void verifyCreateUserProfile(UserProfileCreationResponse resource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull();
        assertThat(resource.getIdamId()).isInstanceOf(String.class);
    }

    protected void verifyGetUserProfile(UserProfileResponse resource, UserProfileCreationData expectedResource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull().isExactlyInstanceOf(String.class);
        assertThat(resource.getFirstName()).isEqualTo(expectedResource.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedResource.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedResource.getEmail().toLowerCase());
        assertThat(resource.getIdamStatus()).isNotNull();
    }

    public UserProfileDataRequest buildUserProfileDataRequest(List<String> userIds) {
        return new UserProfileDataRequest(userIds);
    }
}