package uk.gov.hmcts.reform.userprofileapi.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.controller.advice.ErrorResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.resource.UserProfileCreationData;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.helper.CreateUserProfileTestDataBuilder.buildCreateUserProfileData;

@SpringBootTest(webEnvironment = MOCK)
@Transactional
class CreateNewUserProfileWithIdamErrorsIntTest extends AuthorizationEnabledIntegrationTest {

    @BeforeEach
    public void setUpWireMock() {

        setSidamRegistrationMockWithStatus(BAD_REQUEST.value(), true);
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void should_return_error_and_not_create_user_profile_when_idam_registration_fails() throws Exception {

        auditRepository.deleteAll();
        UserProfileCreationData data = buildCreateUserProfileData();

        MvcResult result =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST
                );

        verifyUserProfileCreation(data);

    }

    @Test
    void should_return_400_when_create_user_profile_has_invalid_role() throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        List<String> roles = new ArrayList<String>();
        roles.add("puicasemanager");
        data.setRoles(roles);
        setSidamRegistrationMockWithStatus(BAD_REQUEST.value(), false);
        ErrorResponse errorResponse =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST,
                        ErrorResponse.class
                );
        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("13 Required parameters or one of request field is missing or invalid");
        assertThat(errorResponse.getErrorDescription()).isEqualTo("Role to be assigned does not exist.");
    }

    @Test
    void should_return_400_when_create_user_profile_has_invalid_role_and_response_is_null_from_sidam()
            throws Exception {

        UserProfileCreationData data = buildCreateUserProfileData();
        List<String> roles = new ArrayList<String>();
        roles.add("puicasemanager");
        data.setRoles(roles);
        setSidamRegistrationMockWithStatus(BAD_REQUEST.value(), true);
        ErrorResponse errorResponse =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST,
                        ErrorResponse.class
                );
        assertThat(errorResponse.getErrorMessage())
                .isEqualTo("13 Required parameters or one of request field is missing or invalid");
        assertThat(errorResponse.getErrorDescription())
                .isEqualTo("13 Required parameters or one of request field is missing or invalid");
    }

    private void verifyUserProfileCreation(UserProfileCreationData data) {

        Optional<UserProfile> optionalUserProfile = userProfileRepository.findByEmail(data.getEmail());
        UserProfile userProfile = optionalUserProfile.orElse(null);
        assertThat(userProfile).isNull();

        List<Audit> audits = auditRepository.findAll();
        if (!CollectionUtils.isEmpty(audits)) {
            audits = audits.stream().sorted((Comparator.comparing(Audit::getAuditTs)).reversed())
                    .collect(Collectors.toList());
            Optional<Audit> optionalAudit = auditRepository.findById(audits.get(0).getId());
            Audit audit = optionalAudit.orElse(null);


            assertThat(audit).isNotNull();
            assertThat(audit.getIdamRegistrationResponse()).isEqualTo(400);
            assertThat(audit.getStatusMessage()).isEqualTo(IdamStatusResolver.INVALID_REQUEST);
            assertThat(audit.getSource()).isEqualTo(ResponseSource.API);
            assertThat(audit.getUserProfile()).isNull();
        }
    }
}
