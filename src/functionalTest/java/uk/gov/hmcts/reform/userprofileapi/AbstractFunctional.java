package uk.gov.hmcts.reform.userprofileapi;

import static java.lang.System.getenv;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildUpdateUserProfileData;

import javax.sql.DataSource;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.RestAssured;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.client.IdamOpenIdClient;
import uk.gov.hmcts.reform.userprofileapi.config.DbConfig;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

@ContextConfiguration(classes = {TestConfigProperties.class, FuncTestRequestHandler.class, DbConfig.class})
@ComponentScan("uk.gov.hmcts.reform.userprofileapi")
@TestPropertySource("classpath:application-functional.yaml")
@TestExecutionListeners(listeners = {
        AbstractFunctional.class,
        DependencyInjectionTestExecutionListener.class})
@Slf4j
public class AbstractFunctional extends AbstractTestExecutionListener {

    @Value("${targetInstance}") protected String targetInstance;

    @Autowired
    protected FuncTestRequestHandler testRequestHandler;

    @Autowired
    protected TestConfigProperties configProperties;

    @Autowired
    private DataSource dataSource;

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
    @Value("${resendInterval}")
    protected String resendInterval;
    protected static IdamOpenIdClient idamOpenIdClient;
    public static final String EMAIL = "EMAIL";

    public static final String CREDS = "CREDS";

    @Override
    public void beforeTestClass(TestContext testContext) {
        testContext.getApplicationContext()
                .getAutowireCapableBeanFactory()
                .autowireBean(this);
        //TO enable for local testing
        /* RestAssured.proxy("proxyout.reform.hmcts.net",8080);
        SerenityRest.proxy("proxyout.reform.hmcts.net", 8080);*/

        RestAssured.baseURI = targetInstance;
        RestAssured.useRelaxedHTTPSValidation();
        idamOpenIdClient = new IdamOpenIdClient(configProperties);
    }

    protected UserProfileCreationResponse createActiveUserProfileWithGivenRoles(HttpStatus expectedStatus,
                                                                                List<String> roles) throws Exception {
        UserProfileCreationData data = createUserProfileData();
        Map<String, String> userCreds = idamOpenIdClient.createUser(roles);
        data.setEmail(userCreds.get(EMAIL));
        return createUserProfile(data, expectedStatus);
    }

    protected UserProfileCreationResponse createUserProfile(UserProfileCreationData userProfileCreationData,
                                                            HttpStatus expectedStatus) throws Exception {

        UserProfileCreationResponse resource = testRequestHandler.sendPost(
                userProfileCreationData,
                expectedStatus,
                requestUri,
                UserProfileCreationResponse.class
        );
        verifyCreateUserProfile(resource);
        return resource;
    }

    protected UserProfileCreationResponse createActiveUserProfile(UserProfileCreationData userProfileCreationData)
            throws Exception {
        List<String> xuiuRoles = new ArrayList();
        xuiuRoles.add("pui-user-manager");
        xuiuRoles.add("pui-case-manager");

        //create user with "pui-user-manager" role in SIDAM
        List<String> sidamRoles = new ArrayList<>();
        sidamRoles.add("pui-user-manager");
        Map<String, String> userCreds = idamOpenIdClient.createUser(sidamRoles);

        //create User profile with same email to get 409 scenario
        userProfileCreationData.setRoles(xuiuRoles);
        userProfileCreationData.setEmail(userCreds.get(EMAIL));
        return createUserProfile(userProfileCreationData, HttpStatus.CREATED);
    }

    protected void updateUserProfile(UpdateUserProfileData updateUserProfileData, String userId,
                                     HttpStatus expectedStatus) throws Exception {

        testRequestHandler.sendPut(
                updateUserProfileData,
                expectedStatus,
                requestUri + "/" + userId);
    }

    protected UserProfileCreationData createUserProfileData() {
        return buildCreateUserProfileData();
    }

    protected UserProfileCreationData createUserProfileDataWithReInvite() {

        return buildCreateUserProfileData(true);
    }

    protected UpdateUserProfileData updateUserProfileData() {
        return buildUpdateUserProfileData();
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

    protected void verifyGetUserProfileWithRoles(UserProfileWithRolesResponse resource,
                                                 UserProfileCreationData expectedResource) {

        verifyGetUserProfile(resource, expectedResource);

    }

    protected UserProfileRolesResponse addRoleRequestWithGivenRoles(Set<RoleName> roles, String idamId)
            throws JsonProcessingException {
        Set<RoleName> rolesToAdd = new HashSet<>();
        rolesToAdd.addAll(roles);

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();
        updateUserProfileData.setRolesAdd(rolesToAdd);
        return addDeleteRole(updateUserProfileData, idamId);
    }

    protected UserProfileRolesResponse deleteRoleRequestWithGivenRoles(Set<RoleName> roles, String idamId)
            throws JsonProcessingException {
        Set<RoleName> rolesToDelete = new HashSet<>();
        rolesToDelete.addAll(roles);

        UpdateUserProfileData updateUserProfileData = new UpdateUserProfileData();
        updateUserProfileData.setRolesDelete(rolesToDelete);
        return addDeleteRole(updateUserProfileData, idamId);
    }

    protected UserProfileRolesResponse addDeleteRole(UpdateUserProfileData updateUserProfileData, String idamId)
            throws JsonProcessingException {
        return testRequestHandler.sendDelete(
                updateUserProfileData,
                HttpStatus.OK,
                requestUri + "/" + idamId,
                UserProfileRolesResponse.class);
    }


    public  UserProfileDataRequest buildUserProfileDataRequest(List<String> userIds) {
        return new UserProfileDataRequest(userIds);
    }

    @Override
    public void afterTestClass(TestContext testContext) {

        deleteTestCaseData();
    }

    private void deleteTestCaseData() {
        String isNightlyBuild = getenv("isNightlyBuild");
        String testUrl = getenv("TEST_URL");
        log.info("isNightlyBuild: {}", isNightlyBuild);
        log.info("testUrl: {}", testUrl);
        if ("true".equalsIgnoreCase(isNightlyBuild) && testUrl.contains("aat")) {
            log.info("Delete test data script execution started");
            try {
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(false);
                ScriptUtils.executeSqlScript(connection,
                        new EncodedResource(new ClassPathResource("delete-functional-test-data.sql")),
                        false, true,
                        ScriptUtils.DEFAULT_COMMENT_PREFIX,
                        ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER,
                        ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
                log.info("Delete test data script execution completed");
            } catch (Exception exe) {
                log.error("Delete test data script execution failed: {}", exe.getMessage());
            }
        } else {
            log.info("Not executing delete test data script");
        }
    }

}