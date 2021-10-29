package uk.gov.hmcts.reform.userprofileapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit5.SerenityTest;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileDataResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileWithRolesResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.RoleName;
import uk.gov.hmcts.reform.userprofileapi.resource.UpdateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.FeatureToggleConditionExtension;
import uk.gov.hmcts.reform.userprofileapi.util.ToggleEnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler.BEARER;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus.ACTIVE;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.generateRandomEmail;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.getIdamRolesJson;
import static uk.gov.hmcts.reform.userprofileapi.util.FeatureToggleConditionExtension.getToggledOffMessage;

@Slf4j
@SerenityTest
@SpringBootTest
@WithTags({@WithTag("testType:Functional")})
public class UserProfileFunctionalTest extends AbstractFunctional {

    public static final String DELETE_USER_BY_ID_OR_EMAIL_PATTERN =
            "UserProfileController.deleteUserProfileByIdOrEmailPattern";

    private ObjectMapper objectMapper;
    private static List<String> endpoints;
    private static UpdateUserProfileData updateUserProfileData;
    private static UserProfileCreationResponse activeUserProfile;
    private static UserProfileCreationResponse pendingUserProfile;
    private static UserProfileCreationData activeUserProfileCreationData;
    private static UserProfileCreationData pendingUserProfileCreationData;

    @Test
    @DisplayName("User profile test scenarios")
    void testUserProfileScenarios() throws Exception {
        setUpTestData();
        createUserScenario();
        findUserByEmailScenario();
        findUserByUserIdScenarios();
        getAllUsersByUserIdsScenario();
        updateUserScenario();
        addOrDeleteUserRolesScenarios();
        reinviteUserScenario();
        deleteUserScenarios();
        endpointSecurityScenarios();
    }

    public void setUpTestData() {
        objectMapper = new ObjectMapper();

        endpoints = of("/v1/userprofile/1", "/v1/userprofile");

        pendingUserProfileCreationData = createUserProfileData();

        activeUserProfileCreationData = new UserProfileCreationData(generateRandomEmail(),
                randomAlphabetic(20), randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false, UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson(), false);

        updateUserProfileData = new UpdateUserProfileData();
        updateUserProfileData.setEmail(activeUserProfileCreationData.getEmail());
        updateUserProfileData.setIdamStatus(ACTIVE.name());
    }

    public void createUserScenario() throws Exception {
        createUserProfileWithIdamDuplicateShouldReturnRolesAndSuccess();
        createDuplicateUserProfileShouldReturnConflict();
    }

    public void updateUserScenario() throws Exception {
        updateUserProfileShouldReturnSuccess();
    }

    public void findUserByEmailScenario() {
        findUserByEmailInHeaderShouldReturnSuccess();
        findUserByEmailInHeaderWithRolesShouldReturnSuccess();
    }

    public void findUserByUserIdScenarios() {
        findUserByUserIdShouldReturnSuccess();
        findUserByUserIdWithRolesShouldReturnSuccess();
    }

    public void getAllUsersByUserIdsScenario() throws Exception {
        getAllUsersByUserIdsWithShowDeletedFalseShouldReturnSuccess();
    }

    public void addOrDeleteUserRolesScenarios() throws JsonProcessingException {
        addRolesToActiveUserProfileShouldReturnSuccess();
        deleteRolesOfActiveUserProfileShouldReturnSuccess();
    }

    public void reinviteUserScenario() throws JsonProcessingException {
        reinviteUserWhenReinvitedWithinOneHourShouldReturn429();
    }

    public void deleteUserScenarios() throws Exception {
        deleteActiveAndPendingUserShouldReturnSuccess();
    }

    public void endpointSecurityScenarios() {
        unauthenticatedRequestsShouldReturn401();
        invalidServiceAuthorisationRequestsShouldReturn401();
        invalidBearerTokenRequestsShouldReturn401();
    }

    public void createUserProfileWithIdamDuplicateShouldReturnRolesAndSuccess() throws Exception {
        log.info("createUserProfileWithIdamDuplicateShouldReturnRolesAndSuccess :: STARTED");

        activeUserProfile = createActiveUserProfileWithGivenFields(activeUserProfileCreationData);
        verifyCreateUserProfile(activeUserProfile);

        UserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");

        log.info("createUserProfileWithIdamDuplicateShouldReturnRolesAndSuccess :: ENDED");
    }

    public void createDuplicateUserProfileShouldReturnConflict() throws Exception {
        log.info("createDuplicateUserProfileShouldReturnConflict :: STARTED");

        activeUserProfile = createDuplicateUserProfileWithGivenFields(activeUserProfileCreationData, CONFLICT);
        verifyCreateUserProfile(activeUserProfile);

        UserProfileWithRolesResponse resource =
                testRequestHandler.sendGet(
                        requestUri + "/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");
    }

    public void updateUserProfileShouldReturnSuccess() throws Exception {
        log.info("updateUserProfileShouldReturnSuccess :: STARTED");

        updateUserProfileData.setFirstName(randomAlphabetic(20));
        updateUserProfileData.setLastName(randomAlphabetic(20));

        updateUserProfile(updateUserProfileData, activeUserProfile.getIdamId());

        UserProfileResponse resource = testRequestHandler.sendGet(
                requestUri + "?userId=" + activeUserProfile.getIdamId(), UserProfileResponse.class);

        verifyUpdatedUserProfile(resource, updateUserProfileData);

        log.info("updateUserProfileShouldReturnSuccess :: ENDED");
    }

    public void findUserByEmailInHeaderShouldReturnSuccess() {
        log.info("findUserByEmailInHeaderShouldReturnSuccess :: STARTED");

        UserProfileResponse resource =
                testRequestHandler.getUserProfileByEmailFromHeader(
                        requestUri,
                        UserProfileResponse.class,
                        activeUserProfileCreationData.getEmail().toLowerCase());

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        log.info("findUserByEmailInHeaderShouldReturnSuccess :: ENDED");
    }

    public void findUserByUserIdShouldReturnSuccess() {
        log.info("findUserByUserIdShouldReturnSuccess :: STARTED");

        UserProfileResponse resource = testRequestHandler.sendGet(
                requestUri + "?userId=" + activeUserProfile.getIdamId(),
                UserProfileResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        log.info("findUserByUserIdShouldReturnSuccess :: ENDED");
    }

    public void findUserByUserIdWithRolesShouldReturnSuccess() {
        log.info("findUserByUserIdWithRolesShouldReturnSuccess :: STARTED");

        UserProfileResponse resource = testRequestHandler.sendGet(
                requestUri + "/" + activeUserProfile.getIdamId() + "/roles",
                UserProfileWithRolesResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");

        log.info("findUserByUserIdWithRolesShouldReturnSuccess :: ENDED");
    }

    public void findUserByEmailInHeaderWithRolesShouldReturnSuccess() {
        log.info("findUserByEmailInHeaderWithRolesShouldReturnSuccess :: STARTED");

        UserProfileResponse resource = testRequestHandler.getUserByEmailInHeaderWithRoles(
                requestUri + "/roles",
                activeUserProfileCreationData.getEmail().toLowerCase(),
                OK,
                UserProfileWithRolesResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");

        log.info("findUserByEmailInHeaderWithRolesShouldReturnSuccess :: ENDED");
    }

    public void getAllUsersByUserIdsWithShowDeletedFalseShouldReturnSuccess() throws Exception {
        log.info("getAllUsersByUserIdsWithShowDeletedFalseShouldReturnSuccess :: STARTED");

        pendingUserProfile = createUserProfile(pendingUserProfileCreationData, HttpStatus.CREATED);

        verifyCreateUserProfile(pendingUserProfile);

        UserProfileDataRequest request = new UserProfileDataRequest(asList(activeUserProfile.getIdamId(),
                pendingUserProfile.getIdamId()));

        UserProfileDataResponse response = testRequestHandler.sendPost(
                request, HttpStatus.OK, requestUri + "/users?showdeleted=false&rolesRequired=true",
                UserProfileDataResponse.class);

        assertThat(response.getUserProfiles().size()).isEqualTo(2);

        log.info("getAllUsersByUserIdsWithShowDeletedFalseShouldReturnSuccess :: ENDED");
    }

    public void addRolesToActiveUserProfileShouldReturnSuccess() throws JsonProcessingException {
        log.info("addRolesToActiveUserProfileShouldReturnSuccess :: STARTED");

        Set<RoleName> rolesName = new HashSet<>();
        rolesName.add(new RoleName(testConfigProperties.getPuiFinanceManager()));
        updateUserProfileData.setRolesAdd(rolesName);

        UserProfileResponse resource =
                testRequestHandler.getUserProfileByEmailFromHeader(
                        requestUri,
                        UserProfileResponse.class,
                        updateUserProfileData.getEmail());

        testRequestHandler.sendPut(updateUserProfileData, OK,
                requestUri + "/" + resource.getIdamId(), UserProfileRolesResponse.class);

        UserProfileWithRolesResponse profileWithRolesResponse =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(profileWithRolesResponse.getRoles().size()).isEqualTo(3);
        assertThat(profileWithRolesResponse.getRoles())
                .containsExactlyInAnyOrderElementsOf(List.of("pui-finance-manager", "pui-user-manager",
                        "pui-case-manager"));
        log.info("addRolesToActiveUserProfileShouldReturnSuccess :: ENDED");
    }

    public void deleteRolesOfActiveUserProfileShouldReturnSuccess() throws JsonProcessingException {
        log.info("deleteRolesOfActiveUserProfileShouldReturnSuccess :: STARTED");

        UserProfileWithRolesResponse profileWithRolesResponse =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(profileWithRolesResponse.getRoles().size()).isNotZero();
        assertThat(profileWithRolesResponse.getRoles().size()).isEqualTo(3);
        assertThat(profileWithRolesResponse.getRoles())
                .containsExactlyInAnyOrderElementsOf(List.of("pui-finance-manager", "pui-user-manager",
                        "pui-case-manager"));

        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(new RoleName(testConfigProperties.getPuiOrgManager()));

        UpdateUserProfileData userProfileDataDelete = new UpdateUserProfileData();
        userProfileDataDelete.setRolesDelete(rolesDelete);

        testRequestHandler.sendDelete(
                userProfileDataDelete, HttpStatus.OK,
                requestUri + "/" + activeUserProfile.getIdamId(), UserProfileRolesResponse.class);

        UserProfileWithRolesResponse resourceForDeleteCheck =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resourceForDeleteCheck.getRoles().size()).isNotZero();
        assertThat(resourceForDeleteCheck.getRoles().size()).isEqualTo(3);
        assertThat(resourceForDeleteCheck.getRoles())
                .containsExactlyInAnyOrderElementsOf(List.of("pui-finance-manager", "pui-user-manager",
                        "pui-case-manager"));
        assertFalse(resourceForDeleteCheck.getRoles().contains(testConfigProperties.getPuiOrgManager()));

        log.info("deleteRolesOfActiveUserProfileShouldReturnSuccess :: ENDED");
    }

    public void reinviteUserWhenReinvitedWithinOneHourShouldReturn429() throws JsonProcessingException {
        log.info("reinviteUserReturn429WhenReinvitedWithinOneHour :: STARTED");

        UserProfileCreationData data = createUserProfileDataWithReInvite();
        data.setEmail(pendingUserProfileCreationData.getEmail());

        ErrorResponse errorResponse = testRequestHandler.sendPost(
                testRequestHandler.asJsonString(data),
                HttpStatus.TOO_MANY_REQUESTS,
                requestUri).as(ErrorResponse.class);

        assertThat(errorResponse.getErrorMessage()).isEqualTo(
                String.format("10 : The request was last made less than %s minutes ago. Please try after some time",
                        testConfigProperties.getResendInterval()));

        log.info("reinviteUserReturn429WhenReinvitedWithinOneHour :: ENDED");
    }

    public void deleteActiveAndPendingUserShouldReturnSuccess() throws JsonProcessingException {
        log.info("deleteActiveUserShouldReturnSuccess :: STARTED");

        List<String> userIds = asList(activeUserProfile.getIdamId(), pendingUserProfile.getIdamId());

        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);

        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                NO_CONTENT, requestUri);

        testRequestHandler.getUserProfileResponse(NOT_FOUND,
                requestUri + "?userId=" + userIds.get(0));

        log.info("deleteActiveUserShouldReturnSuccess :: ENDED");
    }

    public void unauthenticatedRequestsShouldReturn401() {
        log.info("unauthenticatedRequestsShouldReturn401 :: STARTED");

        endpoints.forEach(callbackEndpoint ->
                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(testConfigProperties.getTargetInstance())
                        .when()
                        .get(callbackEndpoint)
                        .then()
                        .statusCode(HttpStatus.UNAUTHORIZED.value()));

        log.info("unauthenticatedRequestsShouldReturn401 :: ENDED");
    }

    public void invalidServiceAuthorisationRequestsShouldReturn401() {
        log.info("invalidServiceAuthorisationRequestsShouldReturn401 :: STARTED");

        endpoints.forEach(endpoint ->
                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(testConfigProperties.getTargetInstance())
                        .header("ServiceAuthorization", "invalidServiceToken")
                        .when()
                        .get(endpoint)
                        .then()
                        .statusCode(HttpStatus.UNAUTHORIZED.value()));

        log.info("invalidServiceAuthorisationRequestsShouldReturn401 :: ENDED");
    }

    public void invalidBearerTokenRequestsShouldReturn401() {
        log.info("invalidBearerTokenRequestsShouldReturn401 :: STARTED");

        SerenityRest
                .given()
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .baseUri(testConfigProperties.getTargetInstance())
                .header("ServiceAuthorization", BEARER + s2sToken)
                .header("Authorization", BEARER + "invalidBearerToken")
                .when()
                .get(requestUri + "?userId=" + UUID.randomUUID())
                .then()
                .log().all(true)
                .statusCode(HttpStatus.UNAUTHORIZED.value()).extract().response();

        log.info("invalidBearerTokenRequestsShouldReturn401 :: ENDED");
    }

    @Test
    @ExtendWith(FeatureToggleConditionExtension.class)
    @ToggleEnable(mapKey = DELETE_USER_BY_ID_OR_EMAIL_PATTERN, withFeature = false)
    @DisplayName("Delete active user by email pattern, should be ignored when feature is toggled off")
    void deleteActiveUserByEmailPatternShouldReturnFailureWhenToggledOff() {
        log.info("deleteActiveUserByEmailPatternShouldReturnFailureWhenToggledOff :: STARTED");

        Response response = testRequestHandler
                .sendDeleteWithoutBody(requestUri + "/users?emailPattern=@prdfunctestuser.com");

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(getToggledOffMessage());

        log.info("deleteActiveUserByEmailPatternShouldReturnFailureWhenToggledOff :: ENDED");
    }

    @DisplayName("Delete active user by id, should run this test only when feature is toggled on")
    @ToggleEnable(mapKey = DELETE_USER_BY_ID_OR_EMAIL_PATTERN, withFeature = true)
    public static void deleteActiveUserByIdShouldReturnSuccess() throws Exception {
        log.info("deleteActiveUserByIdShouldReturnSuccess :: STARTED");

        UserProfileCreationData userProfileCreationData = new UserProfileCreationData(generateRandomEmail(),
                randomAlphabetic(20), randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false, UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson(), false);

        UserProfileCreationResponse activeUserProfile =
                createActiveUserProfileWithGivenFields(userProfileCreationData);
        verifyCreateUserProfile(activeUserProfile);

        Response response = testRequestHandler
                .sendDeleteWithoutBody(requestUri + "/users?userId=" + activeUserProfile.getIdamId());

        if (NO_CONTENT.value() == response.statusCode()) {
            testRequestHandler.getUserProfileResponse(
                    NOT_FOUND, requestUri + "?userId=" + activeUserProfile.getIdamId());
        } else {
            log.info("deleteActiveUserByIdShouldReturnSuccess ::"
                    + " delete response status code: " + response.statusCode());
        }

        log.info("deleteActiveUserByIdShouldReturnSuccess :: ENDED");
    }

    @DisplayName("Delete active user by email pattern, should run this test only when feature is toggled on")
    @ToggleEnable(mapKey = DELETE_USER_BY_ID_OR_EMAIL_PATTERN, withFeature = true)
    public static void deleteActiveUserByEmailPatternShouldReturnSuccess() throws Exception {
        log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess :: STARTED");

        UserProfileCreationData userProfileCreationData = new UserProfileCreationData(generateRandomEmail(),
                randomAlphabetic(20), randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false, UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson(), false);

        UserProfileCreationResponse activeUserProfile =
                createActiveUserProfileWithGivenFields(userProfileCreationData);
        verifyCreateUserProfile(activeUserProfile);

        Response response = testRequestHandler
                .sendDeleteWithoutBody(requestUri + "/users?emailPattern=@prdfunctestuser.com");

        if (NO_CONTENT.value() == response.statusCode()) {
            testRequestHandler.getUserProfileResponse(
                    NOT_FOUND, requestUri + "?userId=" + activeUserProfile.getIdamId());
        } else {
            log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess ::"
                    + " delete response status code: " + response.statusCode());
        }

        log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess :: ENDED");
    }

    @AfterAll
    public static void cleanUpTestData() {
        try {
            deleteActiveUserByIdShouldReturnSuccess();
            deleteActiveUserByEmailPatternShouldReturnSuccess();
        } catch (Exception e) {
            log.error("cleanUpTestData :: threw the following exception: " + e);
        }
    }
}
