package uk.gov.hmcts.reform.userprofileapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.rest.SerenityRest;
import net.thucydides.core.annotations.WithTag;
import net.thucydides.core.annotations.WithTags;
import org.junit.Test;
import org.junit.runner.RunWith;
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
import uk.gov.hmcts.reform.userprofileapi.util.CustomSerenityRunner;
import uk.gov.hmcts.reform.userprofileapi.util.FeatureConditionEvaluation;
import uk.gov.hmcts.reform.userprofileapi.util.ToggleEnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.springframework.http.HttpStatus.*;
import static uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler.BEARER;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus.ACTIVE;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.generateRandomEmail;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.getIdamRolesJson;

@Slf4j
@RunWith(CustomSerenityRunner.class)
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
    public void testUserProfileScenarios() throws Exception {
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
        findUserByEmailInQueryParamShouldReturnSuccess();
        findUserByEmailInHeaderShouldReturnSuccess();
        findUserWithNoEmailInHeaderAndQueryParamShouldReturnBadRequest();
        findUserByEmailInQueryParamWithRolesShouldReturnSuccess();
        findUserByEmailInHeaderWithRolesShouldReturnSuccess();
        findUserByNoEmailInHeaderAndQueryParamWithRolesShouldReturnBadRequest();
    }

    public void findUserByUserIdScenarios() {
        findUserByNonExistentUserIdWithRolesShouldReturnNotFound();
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

    /**
     * Test Methods below.
     */

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

    public void findUserByEmailInQueryParamShouldReturnSuccess() {
        log.info("findUserByEmailInQueryParamShouldReturnSuccess :: STARTED");

        UserProfileResponse resource =
                testRequestHandler.getUserProfileByEmailFromQueryParam(
                        requestUri + "?email=" + activeUserProfileCreationData.getEmail().toLowerCase(),
                        UserProfileResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        log.info("findUserByEmailInQueryParamShouldReturnSuccess :: ENDED");
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

    public void findUserWithNoEmailInHeaderAndQueryParamShouldReturnBadRequest() {
        log.info("findUserWithNoEmailInHeaderAndQueryParamShouldReturnBadRequest :: STARTED");

        testRequestHandler.getUserProfileWithNoEmail(requestUri, HttpStatus.BAD_REQUEST);

        log.info("findUserWithNoEmailInHeaderAndQueryParamShouldReturnBadRequest :: ENDED");
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

    public void findUserByNonExistentUserIdWithRolesShouldReturnNotFound() {
        log.info("findUserByNonExistentUserIdWithRolesShouldReturnNotFound :: STARTED");

        final String nonExistentId = UUID.randomUUID().toString();

        testRequestHandler.getUserByNonExistentId(
                requestUri + "/" + nonExistentId + "/roles",
                NOT_FOUND);

        log.info("findUserByNonExistentUserIdWithRolesShouldReturnNotFound :: ENDED");
    }

    public void findUserByEmailInQueryParamWithRolesShouldReturnSuccess() {
        log.info("findUserByEmailWithRolesShouldReturnSuccess :: STARTED");

        UserProfileResponse resource = testRequestHandler.sendGet(
                requestUri + "/roles?email=" + activeUserProfileCreationData.getEmail().toLowerCase(),
                UserProfileWithRolesResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        assertThat(resource.getRoles()).contains("pui-case-manager");
        assertThat(resource.getRoles()).contains("pui-user-manager");

        log.info("findUserByEmailWithRolesShouldReturnSuccess :: ENDED");
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

    public void findUserByNoEmailInHeaderAndQueryParamWithRolesShouldReturnBadRequest() {
        log.info("findUserByNoEmailInHeaderAndQueryParamWithRolesShouldReturnBadRequest :: STARTED");

        final Response response =
                testRequestHandler.getUserProfileWithNoEmail(requestUri + "/roles", HttpStatus.BAD_REQUEST);

        response.then().body("errorDescription", equalTo("No User Email provided via header or param"));

        log.info("findUserByNoEmailInHeaderAndQueryParamWithRolesShouldReturnBadRequest :: ENDED");
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
        rolesName.add(new RoleName(puiFinanceManager));
        updateUserProfileData.setRolesAdd(rolesName);

        UserProfileResponse resource =
                testRequestHandler.getUserProfileByEmailFromQueryParam(requestUri + "?email=" + updateUserProfileData.getEmail(),
                        UserProfileResponse.class);

        testRequestHandler.sendPut(updateUserProfileData, OK,
                requestUri + "/" + resource.getIdamId(), UserProfileRolesResponse.class);

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource2.getRoles().size()).isEqualTo(3);
        assertThat(resource2.getRoles().contains("pui-finance-manager,pui-case-manager,pui-user-manager"));

        log.info("addRolesToActiveUserProfileShouldReturnSuccess :: ENDED");
    }

    public void deleteRolesOfActiveUserProfileShouldReturnSuccess() throws JsonProcessingException {
        log.info("deleteRolesOfActiveUserProfileShouldReturnSuccess :: STARTED");

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource2.getRoles().size()).isNotNull();
        assertThat(resource2.getRoles().size()).isEqualTo(3);
        assertThat(resource2.getRoles().contains("pui-finance-manager,pui-case-manager,pui-user-manager"));

        Set<RoleName> rolesDelete = new HashSet<>();
        rolesDelete.add(new RoleName(puiOrgManager));

        UpdateUserProfileData userProfileDataDelete = new UpdateUserProfileData();
        userProfileDataDelete.setRolesDelete(rolesDelete);

        testRequestHandler.sendDelete(
                userProfileDataDelete, HttpStatus.OK,
                requestUri + "/" + activeUserProfile.getIdamId(), UserProfileRolesResponse.class);

        UserProfileWithRolesResponse resourceForDeleteCheck =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resourceForDeleteCheck.getRoles().size()).isNotNull();
        assertThat(resourceForDeleteCheck.getRoles().size()).isEqualTo(3);
        assertThat(resourceForDeleteCheck.getRoles().contains("pui-finance-manager,pui-case-manager,pui-user-manager"));
        assertThat(!resourceForDeleteCheck.getRoles().contains(puiOrgManager));

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
                        resendInterval));

        log.info("reinviteUserReturn429WhenReinvitedWithinOneHour :: ENDED");
    }

    public void deleteActiveAndPendingUserShouldReturnSuccess() throws JsonProcessingException {
        log.info("deleteActiveUserShouldReturnSuccess :: STARTED");

        List<String> userIds = asList(activeUserProfile.getIdamId(), pendingUserProfile.getIdamId());

        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);

        testRequestHandler.sendDelete(
                objectMapper.writeValueAsString(deletionRequest),
                NO_CONTENT, requestUri);

        testRequestHandler.sendGet(NOT_FOUND,
                requestUri + "?userId=" + userIds.get(0));

        log.info("deleteActiveUserShouldReturnSuccess :: ENDED");
    }

    public void unauthenticatedRequestsShouldReturn401() {
        log.info("unauthenticatedRequestsShouldReturn401 :: STARTED");

        endpoints.forEach(callbackEndpoint ->
                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(targetInstance)
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
                        .baseUri(targetInstance)
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
                .baseUri(targetInstance)
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
    @ToggleEnable(mapKey = DELETE_USER_BY_ID_OR_EMAIL_PATTERN, withFeature = true)
    public void deleteActiveUserByIdShouldReturnSuccess() throws Exception {
        log.info("deleteActiveUserByIdShouldReturnSuccess :: STARTED");

        UserProfileCreationData userProfileCreationData = new UserProfileCreationData(generateRandomEmail(),
                randomAlphabetic(20), randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false, UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson(), false);

        UserProfileCreationResponse activeUserProfile =
                createActiveUserProfileWithGivenFields(userProfileCreationData);
        verifyCreateUserProfile(activeUserProfile);

        Response response = testRequestHandler
                .sendDeleteWithoutBody(NO_CONTENT, requestUri + "/users?userId=" + activeUserProfile.getIdamId());

        assertThat(response.getStatusCode()).isEqualTo(204);

        testRequestHandler.sendGet(NOT_FOUND, requestUri + "?userId=" + activeUserProfile.getIdamId());

        log.info("deleteActiveUserByIdShouldReturnSuccess :: ENDED");
    }

    @Test
    @ToggleEnable(mapKey = DELETE_USER_BY_ID_OR_EMAIL_PATTERN, withFeature = true)
    public void deleteActiveUserByEmailPatternShouldReturnSuccess() throws Exception {
        log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess :: STARTED");

        UserProfileCreationData userProfileCreationData = new UserProfileCreationData(generateRandomEmail(),
                randomAlphabetic(20), randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false, UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson(), false);

        UserProfileCreationResponse activeUserProfile =
                createActiveUserProfileWithGivenFields(userProfileCreationData);
        verifyCreateUserProfile(activeUserProfile);

        Response response = testRequestHandler
                .sendDeleteWithoutBody(NO_CONTENT, requestUri + "/users?emailPattern=@prdfunctestuser.com");

        assertThat(response.getStatusCode()).isEqualTo(204);

        testRequestHandler.sendGet(NOT_FOUND, requestUri + "?userId=" + activeUserProfile.getIdamId());

        log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess :: ENDED");
    }

    @Test
    @ToggleEnable(mapKey = DELETE_USER_BY_ID_OR_EMAIL_PATTERN, withFeature = false)
    public void deleteActiveUserByEmailPatternShouldReturnFailureWhenToggledOff() {
        log.info("deleteActiveUserByEmailPatternShouldReturnFailureWhenToggledOff :: STARTED");

        Response response = testRequestHandler
                .sendDeleteWithoutBody(NO_CONTENT, requestUri + "/users?emailPattern=@prdfunctestuser.com");

        assertThat(HttpStatus.FORBIDDEN.value()).isEqualTo(response.statusCode());
        assertThat(response.getBody().asString()).contains(CustomSerenityRunner.getFeatureFlagName().concat(" ")
                .concat(FeatureConditionEvaluation.FORBIDDEN_EXCEPTION_LD));

        log.info("deleteActiveUserByEmailPatternShouldReturnFailureWhenToggledOff :: ENDED");
    }

}
