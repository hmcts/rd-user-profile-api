package uk.gov.hmcts.reform.userprofileapi;

import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.State;
import au.com.dius.pact.provider.junitsupport.loader.PactBroker;
import au.com.dius.pact.provider.spring.junit5.MockMvcTestTarget;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Maps;
import feign.Request;
import feign.RequestTemplate;
import feign.Response;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.hmcts.reform.userprofileapi.controller.UserProfileController;
import uk.gov.hmcts.reform.userprofileapi.controller.request.UserProfileDataRequest;
import uk.gov.hmcts.reform.userprofileapi.controller.response.AttributeResponse;
import uk.gov.hmcts.reform.userprofileapi.controller.response.UserProfilesDeletionResponse;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRegistrationInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.IdamRolesInfo;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfile;
import uk.gov.hmcts.reform.userprofileapi.domain.entities.UserProfileIdamStatus;
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
import uk.gov.hmcts.reform.userprofileapi.service.impl.DeleteUserProfileServiceImpl;
import uk.gov.hmcts.reform.userprofileapi.service.impl.IdamServiceImpl;
import uk.gov.hmcts.reform.userprofileapi.service.impl.UserProfileService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static java.nio.charset.Charset.defaultCharset;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@Provider("rd_user_profile_api_service")
@PactBroker(scheme = "${PACT_BROKER_SCHEME:http}", host = "${PACT_BROKER_URL:localhost}",
        port = "${PACT_BROKER_PORT:9292}")
@Import(UserProfileProviderTestConfiguration.class)
public class UserProfileProviderTest {

    @Autowired
    private UserProfileService<RequestData> userProfileService;

    @Mock
    private DeleteUserProfileServiceImpl deleteUserProfileService;

    private IdamRolesInfo idamRolesInfo;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private IdamService idamService;

    @Autowired
    private IdamFeignClient idamClient;

    @Autowired
    private ValidationService validationService;

    @Mock
    IdamServiceImpl idamServiceMock;

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
        IdamRolesInfo idamRolesInfo = new IdamRolesInfo("007", "test@test.com", "testFN",
                "testSN", Collections.singletonList("Secret-Agent"), true, false,
                HttpStatus.OK,"11 OK");
        doReturn(idamRolesInfo).when(idamService).fetchUserById(any());
    }

    //@State({"A user profile with get request for roles"})
    public void getUserProfileForRoles() {
        Supplier<Optional<UserProfile>> up = () -> Optional.of(genUserProfile());
        doReturn(up).when(querySupplier).getRetrieveByIdQuery(any());
        IdamRolesInfo idamRolesInfo = new IdamRolesInfo("007", "test@test.com", "testFN",
                "testSN", Collections.singletonList("Secret Agent"), true, false,
                HttpStatus.OK,"11 OK");
        doReturn(idamRolesInfo).when(idamService).fetchUserById(any());
    }


    //@State({"A user profile retrieve request is submitted"})
    public void retrieveUserProfile() {
        Supplier<Optional<UserProfile>> up = () -> Optional.of(genUserProfile());
        doReturn(up).when(querySupplier).getRetrieveByIdQuery(any());
        List<UserProfile> userProfiles = Collections.singletonList(genUserProfile());

        when(userProfileRepository.findByIdamIdIn(anyList())).thenReturn(Optional.of(userProfiles));
    }

    //@State({"Retrieve multiple user profiles"})
    public void retrieveMultipleUserProfile() throws JsonProcessingException {

        var userProfiles = Collections.singletonList(genUserProfile());
        when(userProfileRepository.findByIdamIdIn(anyList())).thenReturn(Optional.of(userProfiles));

    }

    //@State({"A user profile Idam Status request"})
    public void retrieveUserProfileIdamStatus() {
        List<UserProfileIdamStatus> userProfileIdamStatuses = new ArrayList<>();
        UserProfileIdamStatus status = buildIdamStatus();
        userProfileIdamStatuses.add(status);
        doReturn(userProfileIdamStatuses).when(querySupplier).getProfilesByUserCategory(any());
        when(userProfileRepository.findByUserCategory(UserCategory.PROFESSIONAL)).thenReturn(userProfileIdamStatuses);
    }


    @State({"A user profile update request is submitted for roles"})
    public void updateUserProfile() {
        Optional<UserProfile> userProfileOptional = Optional.of(genUserProfile());
        AttributeResponse attributeResponse = new AttributeResponse(ResponseEntity.status(200).build());
        doReturn(attributeResponse).when(idamService).updateUserDetails(any(), any());
        doReturn(userProfileOptional).when(userProfileRepository).findByIdamId(any());

        doReturn(Response.builder().status(200).body("Success", defaultCharset())
                .request(userProfileRequest(Request.HttpMethod.PUT)).build())
                .when(idamClient).addUserRoles(any(),anyString());

        doReturn(Response.builder().status(200).body("Success", defaultCharset())
                .request(userProfileRequest(Request.HttpMethod.DELETE)).build())
                .when(idamClient).deleteUserRole(any(),anyString());
    }

    @State({"A user profile create request is submitted"})
    public void createUserProfile() {
        IdamRegistrationInfo idamRegistrationInfo =
                new IdamRegistrationInfo(HttpStatus.OK,"OK", ResponseEntity.accepted().build());
        doReturn(idamRegistrationInfo).when(idamService).registerUser(any());
    }


    //@State({"A user profile delete request"})
    public void deleteUserProfile() {

        UserProfileDataRequest identifier = mock(UserProfileDataRequest.class);
        UserProfilesDeletionResponse userProfilesDeletionResponse =
                new UserProfilesDeletionResponse(204, "UserProfiles Successfully Deleted");

        when(deleteUserProfileService.delete(identifier)).thenReturn(userProfilesDeletionResponse);
    }


    private Request userProfileRequest(Request.HttpMethod httpMethod) {
        return Request.create(httpMethod, "url", getResponseHeaders(), Request.Body.empty(),
                new RequestTemplate());
    }

    private UserProfile genUserProfile() {
        return new UserProfile(007L,"007","test@test.com", "testFN", "testSN",
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

    @NotNull
    private static UserProfileIdamStatus buildIdamStatus() {
        return new UserProfileIdamStatus() {
            @Override
            public String getEmail() {
                return "test@email.com";
            }

            @Override
            public String getStatus() {
                return "pending";
            }
        };
    }
}
