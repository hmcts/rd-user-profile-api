package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfileCreationResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;
import static uk.gov.hmcts.reform.userprofileapi.integration.wiremock.IdamWireMockStubs.mockWithGetFail;
import static uk.gov.hmcts.reform.userprofileapi.integration.wiremock.IdamWireMockStubs.mockWithGetSuccess;
import static uk.gov.hmcts.reform.userprofileapi.integration.wiremock.IdamWireMockStubs.mockWithUpdateFail;
import static uk.gov.hmcts.reform.userprofileapi.integration.wiremock.IdamWireMockStubs.mockWithUpdateRolesSuccess;
import static uk.gov.hmcts.reform.userprofileapi.integration.wiremock.IdamWireMockStubs.stubRegistrationResponse;

class CreateNewUserProfileWithDuplicateUserIntTest extends AuthorizationEnabledIntegrationTest {

    private final String userId = UUID.randomUUID().toString();

    @BeforeEach
    public void setUpWireMock() {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
        stubRegistrationResponse(userId);
    }


    @Test
    void should_return_201_and_create_user_profile_when_duplicate_in_sidam() throws Exception {
        mockWithGetSuccess(userId, false);
        mockWithUpdateRolesSuccess(userId);
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, IdamStatus.ACTIVE);

    }

    @Test
    void should_return_201_and_create_user_profile_when_status_not_properly_returned_by_sidam()
            throws Exception {
        mockWithGetSuccess(userId, false);
        mockWithUpdateRolesSuccess(userId);
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        CREATED,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreation(createdResource, CREATED, IdamStatus.ACTIVE);

    }

    @Test
    void should_return_404_and_not_create_user_profile_when_duplicate_in_sidam_and_get_failed()
            throws Exception {
        mockWithGetFail(NOT_FOUND, false);
        mockWithUpdateRolesSuccess(userId);
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
        UserProfileCreationData data = buildCreateUserProfileData();

        userProfileRequestHandlerTest.sendPost(
                mockMvc,
                APP_BASE_PATH,
                data,
                NOT_FOUND,
                UserProfileCreationResponse.class
        );
        verifyUserProfileCreationForFailure(NOT_FOUND);

    }

    @Test
    void should_return_400_and_not_create_user_profile_when_duplicate_in_sidam_and_update_failed()
            throws Exception {
        mockWithGetSuccess(true);
        mockWithUpdateFail(userId);
        auditRepository.deleteAll();
        userProfileRepository.deleteAll();
        UserProfileCreationData data = buildCreateUserProfileData();

        UserProfileCreationResponse createdResource =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST,
                        UserProfileCreationResponse.class
                );

        verifyUserProfileCreationForFailure(BAD_REQUEST);

    }

    private void verifyUserProfileCreation(UserProfileCreationResponse createdResource, HttpStatus idamStatus,
                                           IdamStatus expectedIdamStatus) {

        assertThat(createdResource.getIdamId()).isNotNull();
        assertThat(createdResource.getIdamId()).isInstanceOf(String.class);
        assertThat(createdResource.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());

        Optional<UserProfile> persistedUserProfile = userProfileRepository.findByIdamId(createdResource.getIdamId());
        UserProfile userProfile = persistedUserProfile.get();
        assertThat(userProfile.getId()).isNotNull().isExactlyInstanceOf(Long.class);
        assertThat(userProfile.getIdamRegistrationResponse()).isNull();
        assertThat(userProfile.getLanguagePreference()).isEqualTo(LanguagePreference.EN);
        assertThat(userProfile.getUserCategory()).isEqualTo(UserCategory.PROFESSIONAL);
        assertThat(userProfile.getUserType()).isEqualTo(UserType.EXTERNAL);
        assertThat(userProfile.getStatus()).isEqualTo(expectedIdamStatus);
        assertThat(userProfile.isEmailCommsConsent()).isFalse();
        assertThat(userProfile.isPostalCommsConsent()).isFalse();
        assertThat(userProfile.getEmailCommsConsentTs()).isNull();
        assertThat(userProfile.getPostalCommsConsentTs()).isNull();
        assertThat(userProfile.getCreated()).isNotNull();
        assertThat(userProfile.getLastUpdated()).isNotNull();

        List<Audit> matchedAudit = getMatchedAuditRecords(auditRepository.findAll(), userProfile.getIdamId());
        assertThat(matchedAudit.size()).isEqualTo(1);
        Audit audit = matchedAudit.get(0);
        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(201);
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.ACCEPTED);
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile().getIdamId()).isEqualTo(createdResource.getIdamId());
        assertThat(audit.getAuditTs()).isNotNull();

    }

    private void verifyUserProfileCreationForFailure(HttpStatus idamStatus) {

        Iterable<UserProfile> userProfileList = userProfileRepository.findAll();
        assertThat(userProfileList.iterator().hasNext()).isFalse();

        List<Audit> auditList = auditRepository.findAll();
        assertThat(auditList.size()).isEqualTo(1);
        Audit audit = auditList.get(0);

        assertThat(audit).isNotNull();
        assertThat(audit.getIdamRegistrationResponse()).isEqualTo(idamStatus.value());
        assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.resolveStatusAndReturnMessage(idamStatus));
        assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
        assertThat(audit.getUserProfile()).isNull();
        assertThat(audit.getAuditTs()).isNotNull();

    }

}
