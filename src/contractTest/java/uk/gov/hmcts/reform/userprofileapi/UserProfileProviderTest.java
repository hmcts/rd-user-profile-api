package uk.gov.hmcts.reform.userprofileapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.google.common.collect.Maps;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileController;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.IdamStatus;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.LanguagePreference;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserCategory;
import uk.gov.hmcts.reform.userprofileapi.domain.enums.UserType;
import uk.gov.hmcts.reform.userprofileapi.domain.feign.IdamFeignClient;
import uk.gov.hmcts.reform.userprofileapi.repository.UserProfileRepository;
import uk.gov.hmcts.reform.userprofileapi.resource.RequestData;
import uk.gov.hmcts.reform.userprofileapi.service.IdamService;
import uk.gov.hmcts.reform.userprofileapi.service.UserProfileQueryProvider;
import uk.gov.hmcts.reform.userprofileapi.service.ValidationService;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;

import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@ExtendWith(SpringExtension.class)
@Provider("rd_user_profile_api_service")
@PactBroker(url = "${PACT_BROKER_SCHEME:http}, ${PACT_BROKER_URL:localhost}, ${PACT_BROKER_PORT:9292}")
@Import(UserProfileProviderTestConfiguration.class)
public class UserProfileProviderTest {

    @Autowired
    private UserProfileService<RequestData> userProfileService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private IdamService idamService;

    @Autowired
    private IdamFeignClient idamClient;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserProfileQueryProvider querySupplier;


    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void pactVerificationTestTemplate(PactVerificationContext context) {
        context.verifyInteraction();
    }

    @BeforeEach
    void beforeCreate(PactVerificationContext context) {
        MockMvcTestTarget testTarget = new MockMvcTestTarget();
        System.getProperties().setProperty("pact.verifier.publishResults", "true");
        testTarget.setControllers(new UserProfileController(userProfileService, idamService,
                validationService, "preview"));
        context.setTarget(testTarget);
    }

    @State({"A user profile with roles get request is submitted with valid Id"})
    public void getUserProfile() {
        Supplier<Optional<UserProfile>> up = () -> Optional.of(genUserProfile());
        doReturn(up).when(querySupplier).getRetrieveByIdQuery(any());
        IdamRolesInfo idamRolesInfo = new IdamRolesInfo("007", "james.bond@justice.gov.uk", "James", "Bond",
                Collections.singletonList("Secret-Agent"), true, false,HttpStatus.OK,"11 OK");
        doReturn(idamRolesInfo).when(idamService).fetchUserById(any());
    }

    @State({"A user profile update request is submitted for roles"})
    public void updateUserProfile() {
        Optional<UserProfile> userProfileOptional = Optional.of(genUserProfile());

        doReturn(userProfileOptional).when(userProfileRepository).findByIdamId(any());

        doReturn(Response.builder().status(200).body("Success", Charset.defaultCharset())
                .request(userProfileRequest(Request.HttpMethod.PUT)).build())
                .when(idamClient).addUserRoles(any(),anyString());

        doReturn(Response.builder().status(200).body("Success", Charset.defaultCharset())
                .request(userProfileRequest(Request.HttpMethod.DELETE)).build())
                .when(idamClient).deleteUserRole(any(),anyString());
    }

    @State({"A user profile create request is submitted"})
    public void createUserProfile() {
        IdamRegistrationInfo idamRegistrationInfo =
                new IdamRegistrationInfo(HttpStatus.OK,"11 OK", ResponseEntity.accepted().build());
        doReturn(idamRegistrationInfo).when(idamService).registerUser(any());
    }

    private Request userProfileRequest(Request.HttpMethod httpMethod) {
        return Request.create(httpMethod, "url", getResponseHeaders(), Request.Body.empty(),
                new RequestTemplate());
    }

    private UserProfile genUserProfile() {
        return new UserProfile(007L,"007","james.bond@justice.gov.uk", "james", "bond",
                LanguagePreference.EN,true, LocalDateTime.now(),true,
                LocalDateTime.now(), UserCategory.PROFESSIONAL, UserType.INTERNAL,
                IdamStatus.ACTIVE, 1, LocalDateTime.now(),LocalDateTime.now(),null,
                Collections.singletonList("Secret Agent"),"none","200");
    }

    @NotNull
    private Map<String, Collection<String>> getResponseHeaders() {
        Map<String, Collection<String>> responseHeaders = Maps.newHashMap();
        responseHeaders.put("Content-Type",
                Collections.singletonList("application/json"));
        return responseHeaders;
    }
}
