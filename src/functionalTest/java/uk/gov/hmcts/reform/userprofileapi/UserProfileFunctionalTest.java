package uk.gov.hmcts.reform.userprofileapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.junit.spring.integration.SpringIntegrationSerenityRunner;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.microsoft.applicationinsights.boot.dependencies.google.common.collect.ImmutableList.of;
import static java.util.Arrays.asList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static uk.gov.hmcts.reform.userprofileapi.client.FuncTestRequestHandler.BEARER;
import static uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus.ACTIVE;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.generateRandomEmail;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.getIdamRolesJson;

@Slf4j
@RunWith(SpringIntegrationSerenityRunner.class)
@WithTags({@WithTag("testType:Functional")})
public class UserProfileFunctionalTest extends AbstractFunctional {

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
        updateUserScenario();
        findUserByEmailScenario();
        findUserByUserIdScenarios();
        getAllUsersByUserIdsScenario();
        addOrDeleteUserRolesScenarios();
        reinviteUserScenario();
        deleteUserScenarios();
        endpointSecurityScenarios();
    }

    public void setUpTestData() {
        objectMapper = new ObjectMapper();

        endpoints = of("/v1/userprofile/1", "/v1/userprofile");

        pendingUserProfileCreationData = createUserProfileData();

        activeUserProfileCreationData = new UserProfileCreationData(
                generateRandomEmail(), randomAlphabetic(20),
                randomAlphabetic(20), LanguagePreference.EN.toString(),
                false, false,
                UserCategory.PROFESSIONAL.toString(),
                UserType.EXTERNAL.toString(), getIdamRolesJson(), false);

        updateUserProfileData = new UpdateUserProfileData();
        updateUserProfileData.setEmail(activeUserProfileCreationData.getEmail());
        updateUserProfileData.setIdamStatus(ACTIVE.name());
    }

    public void createUserScenario() throws Exception {
        createUserProfileWithIdamDuplicateShouldReturnRolesAndSuccess();
    }

    public void updateUserScenario() throws Exception {
        updateUserProfileShouldReturnSuccess();
    }

    public void findUserByEmailScenario() {
        findUserByEmailWithHeaderShouldReturnSuccess();
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
        addRolesFromHeaderToActiveUserProfileShouldReturnSuccess();
        deleteRolesOfActiveUserProfileShouldReturnSuccess();
    }

    public void reinviteUserScenario() throws JsonProcessingException {
        reinviteUserWhenReinvitedWithinOneHourShouldReturn429();
    }

    public void deleteUserScenarios() throws Exception {
        deletePendingUserShouldReturnSuccess();
        deleteActiveUserByIdShouldReturnSuccess();
        deleteActiveUserByEmailPatternShouldReturnSuccess();
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

    public void updateUserProfileShouldReturnSuccess() throws Exception {
        log.info("updateUserProfileShouldReturnSuccess :: STARTED");

        updateUserProfile(updateUserProfileData, activeUserProfile.getIdamId());

        log.info("updateUserProfileShouldReturnSuccess :: ENDED");
    }

    public void findUserByEmailWithHeaderShouldReturnSuccess() {
        log.info("findUserByEmailShouldReturnSuccess :: STARTED");

        UserProfileResponse resource =
                testRequestHandler.getEmailFromHeader(
                        requestUri + "?email=" + activeUserProfileCreationData.getEmail().toLowerCase(),
                        UserProfileResponse.class,
                        activeUserProfileCreationData.getEmail().toLowerCase());

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        log.info("findUserByEmailShouldReturnSuccess :: ENDED");
    }

    public void findUserByUserIdShouldReturnSuccess() {
        log.info("findUserByUserIdShouldReturnSuccess :: STARTED");

        UserProfileResponse resource = testRequestHandler.sendGet(
                requestUri + "?userId=" + activeUserProfile.getIdamId(), UserProfileResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        log.info("findUserByUserIdShouldReturnSuccess :: ENDED");
    }

    public void findUserByUserIdWithRolesShouldReturnSuccess() {
        log.info("findUserByUserIdWithRolesShouldReturnSuccess :: STARTED");

        UserProfileResponse resource = testRequestHandler.sendGet(
                requestUri + "/" + activeUserProfile.getIdamId() + "/roles",
                UserProfileWithRolesResponse.class);

        verifyGetUserProfile(resource, activeUserProfileCreationData);

        log.info("findUserByUserIdWithRolesShouldReturnSuccess :: ENDED");
    }

    public void getAllUsersByUserIdsWithShowDeletedFalseShouldReturnSuccess() throws Exception {
        log.info("getAllUsersByUserIdsWithShowDeletedFalseShouldReturnSuccess :: STARTED");

        pendingUserProfile = createUserProfile(pendingUserProfileCreationData);

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
                testRequestHandler.sendGet(requestUri + "?email=" + updateUserProfileData.getEmail(),
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

    public void addRolesFromHeaderToActiveUserProfileShouldReturnSuccess() throws JsonProcessingException {
        log.info("addRolesFromHeaderToActiveUserProfileShouldReturnSuccess :: STARTED");

        Set<RoleName> rolesName = new HashSet<>();
        rolesName.add(new RoleName(puiOrgManager));
        updateUserProfileData.setRolesAdd(rolesName);

        UserProfileResponse resource =
                testRequestHandler.getEmailFromHeader(requestUri + "?email=" + " ",
                        UserProfileResponse.class, updateUserProfileData.getEmail());

        testRequestHandler.sendPut(updateUserProfileData, OK,
                requestUri + "/" + resource.getIdamId(), UserProfileRolesResponse.class);

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + resource.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource2.getRoles().size()).isEqualTo(4);
        assertThat(resource2.getRoles()
                .contains("pui-finance-manager,pui-case-manager,pui-org-manager,pui-user-manager"));

        log.info("addRolesFromHeaderToActiveUserProfileShouldReturnSuccess :: ENDED");
    }

    public void deleteRolesOfActiveUserProfileShouldReturnSuccess() throws JsonProcessingException {
        log.info("deleteRolesOfActiveUserProfileShouldReturnSuccess :: STARTED");

        UserProfileWithRolesResponse resource2 =
                testRequestHandler.sendGet(
                        "/v1/userprofile/" + activeUserProfile.getIdamId() + "/roles",
                        UserProfileWithRolesResponse.class);

        assertThat(resource2.getRoles().size()).isNotNull();
        assertThat(resource2.getRoles().size()).isEqualTo(4);
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

    public void deletePendingUserShouldReturnSuccess() throws JsonProcessingException {
        log.info("deletePendingUserShouldReturnSuccess :: STARTED");

        List<String> userIds = asList(pendingUserProfile.getIdamId());

        UserProfileDataRequest deletionRequest = buildUserProfileDataRequest(userIds);

        testRequestHandler.sendDelete(objectMapper.writeValueAsString(deletionRequest), NO_CONTENT, requestUri);

        testRequestHandler.sendGet(NOT_FOUND,
                requestUri + "?userId=" + userIds.get(0));

        log.info("deletePendingUserShouldReturnSuccess :: ENDED");
    }

    public void deleteActiveUserByIdShouldReturnSuccess() {
        log.info("deleteActiveUserByIdShouldReturnSuccess :: STARTED");

        testRequestHandler
                .sendDeleteWithoutBody(NO_CONTENT, requestUri + "/users?userId=" + activeUserProfile.getIdamId());

        testRequestHandler.sendGet(NOT_FOUND, requestUri + "?userId=" + activeUserProfile.getIdamId());

        log.info("deleteActiveUserByIdShouldReturnSuccess :: ENDED");
    }

    public void deleteActiveUserByEmailPatternShouldReturnSuccess() throws Exception {
        log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess :: STARTED");

        activeUserProfile = createActiveUserProfileWithGivenFields(activeUserProfileCreationData);
        verifyCreateUserProfile(activeUserProfile);

        testRequestHandler
                .sendDeleteWithoutBody(NO_CONTENT, requestUri + "/users?emailPattern=@prdfunctestuser.com");

        testRequestHandler.sendGet(NOT_FOUND, requestUri + "?userId=" + activeUserProfile.getIdamId());

        log.info("deleteActiveUsersByEmailPatternShouldReturnSuccess :: ENDED");
    }

    public void unauthenticatedRequestsShouldReturn401() {
        endpoints.forEach(callbackEndpoint ->
                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(targetInstance)
                        .when()
                        .get(callbackEndpoint)
                        .then()
                        .statusCode(HttpStatus.UNAUTHORIZED.value()));
    }

    public void invalidServiceAuthorisationRequestsShouldReturn401() {
        endpoints.forEach(endpoint ->
                SerenityRest.given()
                        .relaxedHTTPSValidation()
                        .baseUri(targetInstance)
                        .header("ServiceAuthorization", "invalidServiceToken")
                        .when()
                        .get(endpoint)
                        .then()
                        .statusCode(HttpStatus.UNAUTHORIZED.value()));
    }

    public void invalidBearerTokenRequestsShouldReturn401() {
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
    }
}