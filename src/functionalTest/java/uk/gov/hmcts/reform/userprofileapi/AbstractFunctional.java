package uk.gov.hmcts.reform.userprofileapi;

import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler;
import uk.gov.hmcts.reform.userprofileapi.client.IdamOpenIdClient;
import uk.gov.hmcts.reform.userprofileapi.client.S2sClient;
import uk.gov.hmcts.reform.userprofileapi.config.TestConfigProperties;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

@Slf4j
@ContextConfiguration(classes = {TestConfigProperties.class})
@ComponentScan("uk.gov.hmcts.reform.userprofileapi")
@TestPropertySource("classpath:application-functional.yaml")
public class AbstractFunctional {

    @Autowired
    protected TestConfigProperties testConfigProperties;

    protected static String requestUri = "/v1/userprofile";
    public static final String EMAIL = "EMAIL";
    public static final String CREDS = "CREDS";
    protected static FuncTestRequestHandler testRequestHandler;
    protected static IdamOpenIdClient idamOpenIdClient;

    protected static String s2sToken;

    @PostConstruct
    public void beforeTestClass() {

        SerenityRest.useRelaxedHTTPSValidation();

        if (null == s2sToken) {
            log.info(":::: Generating S2S Token");
            s2sToken = new S2sClient(
                    testConfigProperties.getS2sBaseUrl(),
                    testConfigProperties.getS2sMicroservice(),
                    testConfigProperties.getS2sSecret())
                    .getS2sToken();
        }

        if (null == idamOpenIdClient) {
            idamOpenIdClient = new IdamOpenIdClient(testConfigProperties);
        }

        testRequestHandler = new FuncTestRequestHandler(testConfigProperties.getTargetInstance(), s2sToken,
                idamOpenIdClient);
    }

    protected static UserProfileCreationResponse createUserProfile(
            UserProfileCreationData userProfileCreationData, HttpStatus httpStatus, String params) throws Exception {

        UserProfileCreationResponse resource = testRequestHandler.sendPost(
                userProfileCreationData,
                httpStatus,
                requestUri + params,
                UserProfileCreationResponse.class
        );
        verifyCreateUserProfile(resource);
        return resource;
    }

    protected static UserProfileCreationResponse createUserProfile(
            UserProfileCreationData userProfileCreationData, HttpStatus httpStatus) throws Exception {
        return createUserProfile(userProfileCreationData, httpStatus, "");
    }

    protected static UserProfileCreationResponse createActiveUserProfileWithGivenFields(
            UserProfileCreationData userProfileCreationData) throws Exception {
        List<String> xuiuRoles = new ArrayList<>();
        xuiuRoles.add("pui-user-manager");
        xuiuRoles.add("pui-case-manager");

        //create user with "pui-user-manager" role in SIDAM
        List<String> sidamRoles = new ArrayList<>();
        sidamRoles.add("pui-user-manager");
        Map<String, String> userCreds = idamOpenIdClient.createUserWithGivenFields(sidamRoles, userProfileCreationData);

        //create User profile with same email to get 409 scenario
        userProfileCreationData.setRoles(xuiuRoles);
        userProfileCreationData.setEmail(userCreds.get(EMAIL));
        return createUserProfile(userProfileCreationData, HttpStatus.CREATED);
    }

    protected static UserProfileCreationResponse createActiveUserProfileWithGivenNames(
            UserProfileCreationData userProfileCreationData) throws Exception {

        //create user with Names in SIDAM
        List<String> sidamRoles = new ArrayList<>();
        sidamRoles.add("role1");
        sidamRoles.add("role2");
        Map<String, String> userCreds = idamOpenIdClient.createUser(sidamRoles);
        userProfileCreationData.setEmail(userCreds.get(EMAIL));
        return createUserProfile(userProfileCreationData, HttpStatus.CREATED, "?origin=SRD");
    }

    protected static Map<String, String> getIdamResponse(String idamId) {
        return idamOpenIdClient.getUser(idamId);
    }

    protected UserProfileCreationResponse createDuplicateUserProfileWithGivenFields(
            UserProfileCreationData userProfileCreationData, HttpStatus expectedStatusCode) throws Exception {
        List<String> xuiuRoles = new ArrayList<>();
        xuiuRoles.add("pui-user-manager");
        xuiuRoles.add("pui-case-manager");

        //create User profile with same email to get 409 scenario
        userProfileCreationData.setRoles(xuiuRoles);
        userProfileCreationData.setEmail(userProfileCreationData.getEmail());
        return createUserProfile(userProfileCreationData, expectedStatusCode);
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

    protected static void verifyCreateUserProfile(UserProfileCreationResponse resource) {

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

    protected void verifyUpdatedUserProfile(UserProfileResponse resource, UpdateUserProfileData expectedResource) {

        assertThat(resource).isNotNull();
        assertThat(resource.getIdamId()).isNotNull().isExactlyInstanceOf(String.class);
        assertThat(resource.getFirstName()).isEqualTo(expectedResource.getFirstName());
        assertThat(resource.getLastName()).isEqualTo(expectedResource.getLastName());
        assertThat(resource.getEmail()).isEqualTo(expectedResource.getEmail().toLowerCase());
        assertThat(resource.getIdamStatus()).isNotNull();
        assertThat(resource.getIdamStatus()).isEqualTo(IdamStatus.ACTIVE.name());
    }

    public UserProfileDataRequest buildUserProfileDataRequest(List<String> userIds) {
        return new UserProfileDataRequest(userIds);
    }
}
