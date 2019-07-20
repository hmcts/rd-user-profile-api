package uk.gov.hmcts.reform.userprofileapi.integration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.reform.userprofileapi.data.CreateUserProfileDataTestBuilder.buildCreateUserProfileData;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import uk.gov.hmcts.reform.userprofileapi.client.CreateUserProfileData;
import uk.gov.hmcts.reform.userprofileapi.client.ResponseSource;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.Audit;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.util.IdamStatusResolver;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = MOCK)
@Transactional
public class CreateNewUserProfileWithIdamErrorsIntTest  extends AuthorizationEnabledIntegrationTest {

    @Before
    public void setUpWireMock() {

        idamService.stubFor(post(urlEqualTo("/user/registration"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withStatus(400)
                ));

        this.mockMvc = webAppContextSetup(webApplicationContext).build();

    }

    @Test
    public void should_return_error_and_not_create_user_profile_when_idam_registration_fails() throws Exception {

        auditRepository.deleteAll();
        CreateUserProfileData data = buildCreateUserProfileData();

        MvcResult result =
                userProfileRequestHandlerTest.sendPost(
                        mockMvc,
                        APP_BASE_PATH,
                        data,
                        BAD_REQUEST
                );

        verifyUserProfileCreation(BAD_REQUEST, data);

    }

    private void verifyUserProfileCreation(HttpStatus idamStatus, CreateUserProfileData data) {

        Optional<UserProfile> optionalUserProfile = userProfileRepository.findByEmail(data.getEmail());
        UserProfile userProfile = optionalUserProfile.orElse(null);
        assertThat(userProfile).isNull();

        List<Audit> audits = auditRepository.findAll();
        if (!CollectionUtils.isEmpty(audits)) {
            audits = audits.stream().sorted((Comparator.comparing(Audit::getAuditTs)).reversed()).collect(Collectors.toList());
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
